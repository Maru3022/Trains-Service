# 📚 Trains Service - Documentation Index

## 🚀 Quick Start

### Для новых разработчиков
1. **Прочитайте**: [README.md](README.md) - Общее описание проекта
2. **Изучите**: [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектура системы
3. **Поймите**: [OUTBOX_PATTERN.md](OUTBOX_PATTERN.md) - Основной паттерн
4. **Запустите**: [DEPLOYMENT.md](DEPLOYMENT.md) - Как развернуть

### Для контрибьютеров
1. **Ознакомьтесь**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
2. **Проверьте**: [COMMIT_CHECKLIST.md](COMMIT_CHECKLIST.md)
3. **Следуйте**: [GIT_COMMIT_GUIDE.md](GIT_COMMIT_GUIDE.md)

---

## 📖 Документация по функциям

### Основные возможности
| Функция | Документация | Код |
|---------|--------------|-----|
| **CRUD Поездов** | [README.md](README.md#🚀-api-эндпоинты) | `TrainController.java` |
| **Outbox Pattern** | [OUTBOX_PATTERN.md](OUTBOX_PATTERN.md) | `OutboxEvent.java`, `OutboxProcessor.java` |
| **Event Messaging** | [ARCHITECTURE.md#жизненный-цикл-события](ARCHITECTURE.md#жизненный-цикл-события) | `TrainEventProducer.java` |
| **Логирование прогресса** | [README.md](README.md#мониторинг-прогресса) | `ProgressController.java` |
| **Расчеты** | [README.md](README.md#-api-эндпоинты) | `CalculatorService.java` |

---

## 🏗️ Архитектура

### Основные компоненты
- **Models**: `OutboxEvent`, `Train`, `Progress`, `Stats`
- **Repositories**: JPA доступ к БД
- **Services**: Бизнес-логика
  - `TrainService` - Управление поездами
  - `MovementService` - Логирование прогресса
  - `CalculatorService` - Вычисления
  - `OutboxProcessor` - Обработка событий
- **Controllers**: REST API endpoints

### Слои архитектуры
```
REST Controllers (API)
    ↓
Business Services (Logic)
    ↓
Event & Messaging (Outbox Pattern)
    ↓
Repositories (Data Access)
    ↓
Database (PostgreSQL)
```

Подробно: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 🗄️ База данных

### Таблицы
| Таблица | Назначение | Документ |
|---------|-----------|----------|
| `train` | Данные о поездах | [DEPLOYMENT.md](DEPLOYMENT.md) |
| `progress` | История прогресса | [DEPLOYMENT.md](DEPLOYMENT.md) |
| `outbox` | Очередь событий | [OUTBOX_PATTERN.md](OUTBOX_PATTERN.md) |
| `stats` | Статистика | [README.md](README.md) |

### Миграции
- **Flyway/Liquibase**: `src/main/resources/db/migration/`
- **Текущая версия**: `V1_0_0__Initial_Schema.sql`
- **Подробно**: [DEPLOYMENT.md#41-создание-postgresql-базы-данных](DEPLOYMENT.md#41-создание-postgresql-базы-данных)

---

## 🧪 Тестирование

### Тесты
| Класс | Тип | Описание |
|-------|-----|---------|
| `TrainMessagingTest` | Integration | Тест Outbox Pattern |
| `TrainServiceIntegrationTest` | Integration | CRUD операции |
| `MovementServiceIntegrationTest` | Integration | Логирование прогресса |
| `TrainControllerTest` | Unit | REST API endpoints |
| `CalculatorServiceTest` | Unit | Расчеты |
| `MovementServiceTest` | Unit | Мокирование зависимостей |

### Запуск тестов
```bash
mvn test                    # Все тесты
mvn test -Dtest=TrainMessagingTest  # Конкретный тест
```

Подробно: [DEPLOYMENT.md#4-тестирование](DEPLOYMENT.md#4-тестирование)

---

## 🚀 Развертывание

### Локальное
1. Установить Java 21, Maven, PostgreSQL, Kafka
2. Клонировать репо: `git clone ...`
3. Собрать: `mvn clean install`
4. Запустить: `mvn spring-boot:run`

### Docker
```bash
docker compose up -d
# или с мониторингом
docker compose -f docker-compose.yml -f monitoring/docker-compose.monitoring.yml up -d
```

### Production
- Смотрите: [DEPLOYMENT.md#9-production-развертывание](DEPLOYMENT.md#9-production-развертывание)
- Используйте: `application-production.yml`

Подробно: [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 📊 Мониторинг

### Метрики
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Actuator**: http://localhost:8035/actuator

### Доступные метрики
- HTTP запросы и латентность
- JVM метрики (память, GC)
- Kafka метрики (отправленные события)
- Database connection pool

Подробно: [README.md#📊-мониторинг](README.md#📊-мониторинг)

---

## 🛠️ Разработка

### Стек технологий
- **Java**: 21+
- **Spring Boot**: 3.4.2
- **Spring Data JPA**
- **Spring Kafka**
- **PostgreSQL**: 12+
- **Kafka**: 3.0+
- **Maven**: 3.8+

### Зависимости
- **Lombok**: Reduce boilerplate
- **Jackson**: JSON processing
- **Prometheus**: Metrics
- **H2**: Test database

Смотрите: `pom.xml`

---

## 📝 API Документация

### REST Endpoints

#### Train Management
```
GET    /api/trains              - Получить все
GET    /api/trains/{id}         - Получить по ID
POST   /api/trains              - Создать
DELETE /api/trains/{id}         - Удалить
```

#### Progress Logging
```
POST   /api/progress/log        - Логирование прогресса
```

#### Calculator
```
GET    /api/v1/calculator/1rm   - Расчет 1RM
```

#### Stats
```
GET    /api/v1/stats/summary    - Сводка
```

Подробно: [README.md#🚀-api-эндпоинты](README.md#🚀-api-эндпоинты)

---

## 🔧 Конфигурация

### Основные файлы конфигурации
| Файл | Назначение |
|------|-----------|
| `application.yml` | Production конфигурация |
| `application-test.yml` | Test конфигурация |
| `pom.xml` | Maven зависимости и плагины |
| `.env` | Переменные окружения (Docker) |

### Переменные окружения
```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5444/trains_db
SPRING_DATASOURCE_USERNAME=trains_user
SPRING_DATASOURCE_PASSWORD=secret
```

Подробно: [DEPLOYMENT.md#3-конфигурация-переменных-окружения](DEPLOYMENT.md#3-конфигурация-переменных-окружения)

---

## 🐛 Troubleshooting

### Типичные проблемы

| Проблема | Решение |
|----------|---------|
| Connection to PostgreSQL refused | [DEPLOYMENT.md#81-ошибка-подключения-к-postgresql](DEPLOYMENT.md#81-ошибка-подключения-к-postgresql) |
| Kafka bootstrap.servers cannot be empty | [DEPLOYMENT.md#82-ошибка-подключения-к-kafka](DEPLOYMENT.md#82-ошибка-подключения-к-kafka) |
| No qualifying bean of type KafkaTemplate | [DEPLOYMENT.md#83-ошибка-outboxprocessor](DEPLOYMENT.md#83-ошибка-outboxprocessor) |
| Tests fail | [DEPLOYMENT.md#84-тесты-не-проходят](DEPLOYMENT.md#84-тесты-не-проходят) |

Полный гайд: [DEPLOYMENT.md#8-troubleshooting](DEPLOYMENT.md#8-troubleshooting)

---

## 📚 Дополнительные ресурсы

### Официальная документация
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Apache Kafka](https://kafka.apache.org)
- [PostgreSQL](https://www.postgresql.org)
- [Docker](https://docs.docker.com)

### Исходный код
- **Main application**: `src/main/java/com/example/trainsservice/`
- **Tests**: `src/test/java/com/example/trainsservice/`
- **Resources**: `src/main/resources/`
- **Database migrations**: `src/main/resources/db/migration/`

---

## 🔄 CI/CD

### GitHub Actions
- Файл конфигурации: `.github/workflows/main.yml`
- Запускает: `mvn clean install`
- Включает: Compile, Test, Package

### Статус сборки
- https://github.com/yourname/Trains-Service/actions

---

## 🎯 Git Workflow

### Clone & Setup
```bash
git clone https://github.com/yourname/Trains-Service.git
cd Trains-Service
./build.sh  # или build.cmd на Windows
```

### Feature Development
```bash
git checkout -b feature/your-feature
# Работа...
git commit -m "feat: your changes"
git push origin feature/your-feature
# Создать Pull Request
```

### Commit Guidelines
Смотрите: [GIT_COMMIT_GUIDE.md](GIT_COMMIT_GUIDE.md)

---

## 🏆 Best Practices

### Code Quality
1. ✅ Используйте `@Transactional` для atomicity
2. ✅ Добавляйте индексы на часто используемые колонки
3. ✅ Обрабатывайте исключения с логированием
4. ✅ Пишите unit и integration тесты

### Outbox Pattern
1. ✅ Сохраняйте события в одной транзакции с данными
2. ✅ Используйте scheduled processor для отправки
3. ✅ Мониторьте таблицу outbox
4. ✅ Обрабатывайте FAILED события

### Production
1. ✅ Используйте `ddl-auto: validate`
2. ✅ Настройте мониторинг и логирование
3. ✅ Делайте backups БД
4. ✅ Используйте кластеры (Kafka, PostgreSQL)

---

## 📞 Контакты и поддержка

### Вопросы о проекте
- Создайте Issue: https://github.com/yourname/Trains-Service/issues
- Обсудите в discussions

### Документация
- Главный README: [README.md](README.md)
- Архитектура: [ARCHITECTURE.md](ARCHITECTURE.md)
- Deployment: [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 📋 Быстрая навигация

### Для различных задач
- **Добавить новый REST endpoint**: `src/main/java/com/example/trainsservice/controller/`
- **Добавить новый сервис**: `src/main/java/com/example/trainsservice/service/`
- **Добавить новую модель**: `src/main/java/com/example/trainsservice/model/`
- **Добавить тест**: `src/test/java/com/example/trainsservice/`
- **Изменить конфигурацию**: `src/main/resources/application.yml`
- **Добавить SQL миграцию**: `src/main/resources/db/migration/`

---

## ✅ Checklist для новых разработчиков

- [ ] Установлена Java 21
- [ ] Установлен Maven 3.8+
- [ ] Установлен Git
- [ ] Установлены PostgreSQL и Kafka (или Docker)
- [ ] Клонирован репозиторий
- [ ] Прочитана документация (этот файл + README)
- [ ] Собран проект: `mvn clean install`
- [ ] Запущены тесты: `mvn test`
- [ ] Запущено приложение: `mvn spring-boot:run`
- [ ] Проверены Actuator endpoints: http://localhost:8035/actuator

---

**Версия документа**: 1.0  
**Последнее обновление**: 31.03.2026  
**Статус**: ✅ Актуально

---

**Начните с**: [README.md](README.md) → [ARCHITECTURE.md](ARCHITECTURE.md) → [OUTBOX_PATTERN.md](OUTBOX_PATTERN.md) → [DEPLOYMENT.md](DEPLOYMENT.md)
