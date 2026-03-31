# Trains Service - Outbox Pattern Implementation

## Описание

Этот проект реализует микросервис для управления поездами с использованием **Outbox Pattern** для надежной доставки событий в Kafka.

## Архитектура

### Outbox Pattern

Outbox Pattern обеспечивает гарантированную доставку событий в message broker:

1. **Сохранение события в БД**: Когда происходит изменение в БД (например, создание поезда), событие сохраняется в таблице `outbox` в **той же транзакции**.
2. **Асинхронная обработка**: Отдельный процесс (`OutboxProcessor`) периодически запрашивает неотправленные события из таблицы.
3. **Отправка в Kafka**: Событие отправляется в Kafka, и после успешной отправки статус обновляется на `SENT`.
4. **Гарантия доставки**: Если отправка не удалась, событие остается в таблице и будет повторена позже.

### Компоненты

#### Models
- **OutboxEvent**: Сущность для хранения событий в очереди
  - id: Уникальный идентификатор
  - topic: Тема Kafka
  - key: Ключ для сообщения
  - payload: JSON-сериализованное содержимое события
  - status: PENDING, SENT, или FAILED
  - createdAt: Время создания
  - processedAt: Время обработки

#### Services
- **TrainEventProducer**: Сохраняет события в таблицу outbox
- **OutboxProcessor**: Периодически обрабатывает события и отправляет в Kafka
- **TrainService**: Бизнес-логика управления поездами

#### Controllers
- **TrainController**: REST API для операций с поездами
- **ProgressController**: REST API для логирования прогресса
- **CalculatorController**: REST API для расчетов

## Установка и запуск

### Требования
- Java 21
- Maven 3.8+
- PostgreSQL 12+
- Kafka 3.0+

### Переменные окружения

```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5444/trains_db
export SPRING_DATASOURCE_USERNAME=trains_user
export SPRING_DATASOURCE_PASSWORD=secret
export EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
```

### Запуск приложения

```bash
# Полная сборка
mvn clean install

# Запуск приложения
mvn spring-boot:run

# Запуск только тестов
mvn test

# Запуск с профилем test
mvn test -Dspring.profiles.active=test
```

## REST API

### Train Management

#### Получить все поезда
```bash
GET /api/trains
```

#### Получить поезд по ID
```bash
GET /api/trains/{id}
```

#### Создать поезд
```bash
POST /api/trains
Content-Type: application/json

{
  "name": "Express Train",
  "category": "Express",
  "durationMinutes": 120
}
```

#### Удалить поезд
```bash
DELETE /api/trains/{id}
```

### Progress Logging

#### Логирование прогресса
```bash
POST /api/progress/log
Content-Type: application/json

{
  "exerciseId": 1,
  "reps": 10,
  "weight": 100.0
}
```

### Calculator

#### Расчет одного повтора максимум (1RM)
```bash
GET /api/v1/calculator/1rm?weight=100&reps=10
```

## Тестирование

### Unit тесты
```bash
mvn test
```

### Интеграционные тесты
```bash
mvn test -Dspring.profiles.active=test
```

### Тесты с покрытием кода
```bash
mvn test
# Результаты в target/site/jacoco/index.html
```

## Конфигурация

### application.yml
- Настройки БД PostgreSQL
- Настройки Kafka
- Eureka (для service discovery)
- Actuator endpoints

### application-test.yml
- H2 в памяти для тестов
- Отключены Redis, Eureka, scheduling
- Встроенный Kafka

## Мониторинг

### Actuator Endpoints
- `GET /actuator/health` - Статус приложения
- `GET /actuator/info` - Информация
- `GET /actuator/prometheus` - Метрики Prometheus

### Prometheus
Метрики доступны по адресу: `http://localhost:8035/actuator/prometheus`

## Логирование

Логирование настроено через SLF4J с Logback:
- ROOT уровень: INFO
- Application пакет: DEBUG (в тестах: DEBUG)

## Обработка ошибок

### Статусы OutboxEvent
- **PENDING**: Событие создано, ожидает отправки
- **SENT**: Событие успешно отправлено в Kafka
- **FAILED**: Ошибка отправки, требует ручного вмешательства

### OutboxProcessor Retry Logic
- Обрабатывает события каждые 5 секунд
- Логирует успешные и ошибочные отправки
- Сохраняет статус FAILED для дальнейшего анализа

## Проблемы и решения

### Тест TrainMessagingTest не работает
- Используется @ActiveProfiles("test") для изоляции
- Не используется @EmbeddedKafka с test профилем
- Проверяет наличие события в таблице outbox

### Контекст Spring не загружается
- Убедитесь, что PostgreSQL доступна
- Проверьте конфигурацию Eureka в application-test.yml
- Используйте H2 для тестов

## Файлы конфигурации

- `pom.xml` - Зависимости Maven
- `src/main/resources/application.yml` - Основная конфигурация
- `src/test/resources/application-test.yml` - Тестовая конфигурация
- `monitoring/docker-compose.monitoring.yml` - Prometheus и Grafana
- `compose.yaml` - Docker Compose для основных сервисов
- `Dockerfile` - Docker образ приложения

## Жизненный цикл события

1. **Создание** → Event создается в TrainService.saveTrain()
2. **Сохранение** → TrainEventProducer сохраняет в таблицу outbox
3. **Обработка** → OutboxProcessor находит PENDING события
4. **Отправка** → Отправляет в Kafka topic "train-events"
5. **Завершение** → Статус изменяется на SENT

## Зависимости

- Spring Boot 3.4.2
- Spring Data JPA
- Spring Kafka
- PostgreSQL Driver
- H2 Database (для тестов)
- Lombok
- Jackson
- JUnit 5
- Mockito
- Prometheus Client

## Лицензия

MIT
