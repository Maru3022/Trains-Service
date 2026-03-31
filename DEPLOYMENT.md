# Инструкция по развертыванию Trains Service

## Предварительные требования

### Системные требования
- **Java**: OpenJDK 21+
- **Maven**: 3.8.0+
- **Git**: Для клонирования репозитория
- **Docker** (опционально): Для контейнеризации

### Внешние сервисы
- **PostgreSQL**: 12+ (порт 5444)
- **Kafka**: 3.0+ (порт 9092)
- **Redis** (опционально): Для кэширования
- **Eureka Server** (опционально): Для service discovery

---

## 1. Локальное развертывание

### 1.1 Клонирование репозитория

```bash
git clone <repository-url>
cd Trains-Service
```

### 1.2 Сборка проекта

#### С помощью скрипта
```bash
# Linux/Mac
chmod +x build.sh
./build.sh

# Windows
build.cmd
```

#### Вручную
```bash
# Очистка старых артефактов
mvn clean

# Компиляция
mvn compile

# Запуск тестов
mvn test

# Создание JAR
mvn package
```

### 1.3 Подготовка БД

#### Создание PostgreSQL базы данных

```bash
# Подключение к PostgreSQL
psql -U postgres

# Создание базы
CREATE DATABASE trains_db OWNER trains_user;

# Создание пользователя (если не существует)
CREATE USER trains_user WITH PASSWORD 'secret';

# Выход
\q
```

#### Применение миграций

Миграции применяются автоматически при запуске приложения:
- **DDL Mode**: `ddl-auto: update` (разработка)
- **SQL Migration**: `src/main/resources/db/migration/V1_0_0__Initial_Schema.sql`

### 1.4 Запуск приложения

```bash
# Основной профиль (требует PostgreSQL и Kafka)
mvn spring-boot:run

# Или запуск созданного JAR
java -jar target/Trains-Service-0.0.1-SNAPSHOT.jar
```

### 1.5 Проверка статуса

```bash
# Health Check
curl http://localhost:8035/actuator/health

# Application Info
curl http://localhost:8035/actuator/info

# Prometheus метрики
curl http://localhost:8035/actuator/prometheus
```

---

## 2. Docker развертывание

### 2.1 Сборка Docker образа

```bash
# Сборка JAR
mvn clean package -DskipTests

# Сборка Docker образа
docker build -t trains-service:latest .
```

### 2.2 Запуск с Docker Compose

```bash
# Запуск основных сервисов
docker compose up -d

# Запуск с мониторингом
docker compose -f docker-compose.yml -f monitoring/docker-compose.monitoring.yml up -d

# Просмотр логов
docker compose logs -f trains-app

# Остановка
docker compose down
```

### 2.3 Проверка контейнеров

```bash
# Список запущенных контейнеров
docker compose ps

# Логи приложения
docker compose logs trains-app

# Интерактивный доступ
docker compose exec trains-app /bin/bash
```

---

## 3. Конфигурация переменных окружения

### 3.1 Системные переменные

```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5444/trains_db
export SPRING_DATASOURCE_USERNAME=trains_user
export SPRING_DATASOURCE_PASSWORD=secret
export EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
export JAVA_OPTS="-Xmx1024m -Xms512m"
```

### 3.2 Файл .env (для Docker)

```bash
# .env
POSTGRES_DB=trains_db
POSTGRES_USER=trains_user
POSTGRES_PASSWORD=secret
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
EUREKA_SERVER_URL=http://eureka:8761/eureka/
```

---

## 4. Тестирование

### 4.1 Unit тесты

```bash
# Все unit тесты
mvn test

# Конкретный тест
mvn test -Dtest=TrainServiceTest

# Тесты с покрытием кода
mvn clean test jacoco:report
```

### 4.2 Integration тесты

```bash
# Интеграционные тесты
mvn verify

# С test профилем
mvn test -Dspring.profiles.active=test
```

### 4.3 Результаты тестов

```bash
# Просмотр отчета покрытия
open target/site/jacoco/index.html  # macOS
start target/site/jacoco/index.html # Windows
xdg-open target/site/jacoco/index.html # Linux

# Просмотр отчета сборки
open target/site/index.html
```

---

## 5. REST API тестирование

### 5.1 Curl примеры

```bash
# Получить все поезда
curl -X GET http://localhost:8035/api/trains

# Создать поезд
curl -X POST http://localhost:8035/api/trains \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Express Train",
    "category": "Express",
    "durationMinutes": 120
  }'

# Получить поезд по ID
curl -X GET http://localhost:8035/api/trains/1

# Удалить поезд
curl -X DELETE http://localhost:8035/api/trains/1

# Логирование прогресса
curl -X POST http://localhost:8035/api/progress/log \
  -H "Content-Type: application/json" \
  -d '{
    "exerciseId": 1,
    "reps": 10,
    "weight": 100.0
  }'

# Расчет 1RM
curl -X GET "http://localhost:8035/api/v1/calculator/1rm?weight=100&reps=10"
```

### 5.2 Postman коллекция

Импортируйте в Postman:
```bash
# Экспортировать текущую конфигурацию как Postman JSON
# и поместить в docs/postman/trains-api.postman_collection.json
```

---

## 6. Мониторинг и логирование

### 6.1 Prometheus

```bash
# URL Prometheus
http://localhost:9090

# Примеры запросов
- up{job="trains-service"}
- rate(http_server_requests_seconds_count[5m])
- jvm_memory_used_bytes
```

### 6.2 Grafana

```bash
# URL Grafana
http://localhost:3000
# Логин: admin
# Пароль: admin

# Импортируемые дашборды:
- Spring Boot Application Metrics
- JVM Monitoring
- HTTP Requests Performance
```

### 6.3 Логи приложения

```bash
# Просмотр логов через Docker
docker compose logs -f trains-app

# Логирование в файл
# Настраивается в logback-spring.xml
tail -f logs/application.log
```

---

## 7. Управление outbox событиями

### 7.1 SQL запросы для управления

```sql
-- Просмотр всех событий
SELECT * FROM outbox ORDER BY created_at DESC;

-- Просмотр ожидающих событий
SELECT * FROM outbox WHERE status = 'PENDING' ORDER BY created_at ASC;

-- Просмотр ошибок
SELECT * FROM outbox WHERE status = 'FAILED' ORDER BY created_at DESC;

-- Статистика по статусам
SELECT status, COUNT(*) as count FROM outbox GROUP BY status;

-- Ручная переотправка события
UPDATE outbox SET status = 'PENDING' WHERE id = 1;

-- Удаление обработанных событий (старше 7 дней)
DELETE FROM outbox WHERE status = 'SENT' AND processed_at < NOW() - INTERVAL '7 days';

-- Очистка всех FAILED событий (осторожно!)
DELETE FROM outbox WHERE status = 'FAILED' AND processed_at < NOW() - INTERVAL '30 days';
```

### 7.2 Мониторинг outbox

```sql
-- Размер очереди
SELECT COUNT(*) FROM outbox WHERE status = 'PENDING';

-- Средний возраст события
SELECT AVG(EXTRACT(EPOCH FROM (NOW() - created_at))) / 60 as avg_minutes
FROM outbox WHERE status = 'PENDING';

-- События, которые долго ждут
SELECT * FROM outbox 
WHERE status = 'PENDING' 
  AND created_at < NOW() - INTERVAL '1 hour'
ORDER BY created_at;
```

---

## 8. Troubleshooting

### 8.1 Ошибка подключения к PostgreSQL

```
Error: org.postgresql.util.PSQLException: Connection to localhost:5444 refused

Решение:
1. Проверьте, что PostgreSQL запущена
2. Проверьте переменные окружения
3. Проверьте credentials в application.yml
4. Убедитесь, что БД существует
```

### 8.2 Ошибка подключения к Kafka

```
Error: org.apache.kafka.clients.NetworkClient - bootstrap.servers cannot be empty

Решение:
1. Проверьте, что Kafka запущена на порту 9092
2. Установите SPRING_KAFKA_BOOTSTRAP_SERVERS
3. В Docker: убедитесь, что сетевое подключение настроено
```

### 8.3 Ошибка OutboxProcessor

```
Error: No qualifying bean of type KafkaTemplate

Решение:
1. Убедитесь, что KafkaStringTemplateConfig загружена
2. Проверьте, что spring-kafka в зависимостях
3. Смотрите logs для более подробной информации
```

### 8.4 Тесты не проходят

```bash
# Очистить и перестроить
mvn clean -DskipTests install

# Запустить с debug логами
mvn test -X

# Запустить один тест
mvn test -Dtest=TrainMessagingTest#testSendEvent
```

---

## 9. Production развертывание

### 9.1 Pre-production чек-лист

- [ ] Пароли изменены (не "secret")
- [ ] ddl-auto = validate (не update)
- [ ] Логирование уровня WARN или выше
- [ ] Monitoring включен (Prometheus)
- [ ] Backups БД настроены
- [ ] Kafka кластер (не single node)
- [ ] PostgreSQL кластер (не single instance)

### 9.2 Развертывание на сервер

```bash
# 1. Сборка
mvn clean package -DskipTests

# 2. Копирование на сервер
scp target/Trains-Service-*.jar user@server:/opt/app/

# 3. Запуск на сервере
ssh user@server
cd /opt/app
java -Xmx2048m -Xms1024m \
  -Dspring.profiles.active=production \
  -jar Trains-Service-*.jar
```

### 9.3 Systemd сервис (Linux)

```ini
# /etc/systemd/system/trains-service.service
[Unit]
Description=Trains Service
After=network.target

[Service]
Type=simple
User=trains
WorkingDirectory=/opt/trains-service
ExecStart=/usr/bin/java -Xmx2048m -Xms1024m -jar Trains-Service-*.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Включить сервис
sudo systemctl daemon-reload
sudo systemctl enable trains-service
sudo systemctl start trains-service

# Проверить статус
sudo systemctl status trains-service

# Просмотреть логи
sudo journalctl -f -u trains-service
```

---

## 10. Обновление приложения

```bash
# 1. Остановить текущую версию
docker compose down

# 2. Извлечь новую версию
git pull origin main

# 3. Пересобрать Docker образ
docker compose build --no-cache

# 4. Запустить новую версию
docker compose up -d

# 5. Проверить статус
docker compose logs -f trains-app
```

---

## Дополнительные ресурсы

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Apache Kafka**: https://kafka.apache.org
- **PostgreSQL**: https://www.postgresql.org
- **Docker Documentation**: https://docs.docker.com

---

**Версия документа**: 1.0
**Дата обновления**: 31.03.2026
**Статус**: ✅ Актуально
