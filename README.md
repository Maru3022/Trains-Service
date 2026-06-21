 # Trains Service

Сервис тренировок fitness-платформы (в коде "Train" = тренировка/тренировочная программа пользователя, не железнодорожный термин). Хранит CRUD тренировок, логирует подходы и считает агрегированную статистику, предоставляет калькулятор 1ПМ (одноповторного максимума), и участвует в Saga создания пользователя как шаг `TRAINS`, отвечающий за персональный кабинет. Все исходящие события публикуются через Transactional Outbox с устойчивым к гонкам claim-based polling.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791?logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-spring--kafka-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-cache-DC382D?logo=redis)
![Prometheus](https://img.shields.io/badge/Prometheus%20%2B%20Grafana-monitoring-E6522C?logo=prometheus)
![JaCoCo](https://img.shields.io/badge/JaCoCo-coverage%20gate%2080%25-green)

## Что делает сервис

- CRUD тренировок (`/api/trains`): создание, чтение, удаление; каждое создание/удаление публикует доменное событие (`TRAIN_EVENT`) в топик `train-events` через Outbox.
- Калькулятор одноповторного максимума по формуле Эпли (`/api/v1/calculator/1rm`): `1RM = weight × (1 + reps / 30)`.
- Логирование подходов (`/api/progress/log`) и агрегированная статистика (`/api/v1/stats/summary`) — общее число залогированных подходов и суммарный тоннаж (`Σ reps × weight`), считается SQL-агрегатом на стороне БД.
- Участвует в Saga создания пользователя как шаг `TRAINS`: слушает `saga-trains-command`, создаёт (или находит существующую) запись-маркер "Personal cabinet" для пользователя и отвечает оркестратору через Outbox; поддерживает компенсацию удалением созданной записи.
- Включён виртуальный поток (Java Virtual Threads, `spring.threads.virtual.enabled=true`) и отдельный Spring-профиль `bench` для локального нагрузочного тестирования (H2 in-memory, Kafka/Redis автоконфигурация отключена), плюс готовый docker-compose со стеком Prometheus + Grafana для наблюдаемости.

## Архитектура

```text
+------------------+      saga-trains-command       +--------------------------+
| Saga-Orchestrator |-------------------------------->|  Trains Service (8035)   |
+------------------+      saga-trains-response        |                           |
        ^             <----------- (Outbox) -----------|  SagaTrainsCommand-       |
        |                                              |  Listener                 |
        |                                              +-----------+--------------+
        |                                                          |
        |                                                          v
        |                                                    PostgreSQL
        |                                          (Train / Progress / Route /
        |                                           outbox_events, Flyway)
        |
        |             train-events (Outbox -> OutboxPollerService -> Kafka)
        +-------------------------------------------------------------------+
                                                                              v
   Client ---> /api/trains (CRUD) -------------------------------> TrainService -> OutboxEventService
   Client ---> /api/v1/calculator/1rm ----------------------------> CalculatorService (без БД)
   Client ---> /api/progress/log ----------------------------------> MovementService -> Progress
   Client ---> /api/v1/stats/summary ------------------------------> StatisticsService (SQL-агрегат)

   Наблюдаемость: /actuator/prometheus -> Prometheus -> Grafana (docker-compose, monitoring/)
```

## Архитектурные решения

### 1. Transactional Outbox с claim-based polling вместо наивного `findByStatus`

`OutboxPollerService` (`@Scheduled(fixedDelayString = "${outbox.poller.interval-ms:1000}")`) не просто читает события со статусом `PENDING` — он атомарно "захватывает" пачку через нативный SQL-запрос `SELECT ... WHERE status = 'PENDING' ... FOR UPDATE SKIP LOCKED LIMIT :batchSize`, затем сразу переводит их в `PROCESSING` (`markProcessing`). Это исключает гонку при горизонтальном масштабировании сервиса (несколько подов не разберут одно и то же событие дважды) — в отличие от похожего, но более простого Outbox-паттерна в соседних сервисах платформы (`Training_Notification`, `Training-Nutrition`), где poller читает по статусу без блокировки строк. Дополнительно есть самовосстановление: `resetStuckProcessingEvents()` каждый цикл возвращает в `PENDING` события, зависшие в `PROCESSING` дольше 5 минут (под зависший под, не успевший подтвердить отправку), а после исчерпания `maxRetries` (по умолчанию 3) событие переводится в терминальный статус `DEAD_LETTER`, а не повторяется бесконечно.

### 2. Saga-шаг "Personal cabinet" без отдельной модели данных

Шаг `TRAINS` саги создания пользователя не заводит отдельную сущность — `SagaTrainsCommandListener` переиспользует существующую модель `Train` с зарезервированным значением `category = "PERSONAL_CABINET"`. Идемпотентность достигается не отдельным полем `correlationId`/`sagaId`, а доменным поиском `findByUserIdAndCategory(userId, "PERSONAL_CABINET")`: если запись уже существует — она просто возвращается, новая не создаётся. Компенсация (`ROLLBACK`) удаляет созданную запись по `trainId` из данных команды. Решение экономит отдельную таблицу для технического служебного состояния, но платит за это смешением доменной модели (реальные тренировки) и инфраструктурного артефакта (маркер кабинета) в одной JPA-сущности.

### 3. Профиль `bench` для воспроизводимого нагрузочного тестирования

Maven-профиль `bench` + Spring-профиль `bench` (`application-bench.yml`) исключают `KafkaAutoConfiguration` и оба `RedisAutoConfiguration`, подменяют PostgreSQL на H2 in-memory в режиме совместимости (`MODE=PostgreSQL`) и отключают `spring.task.scheduling` — соответственно, `OutboxPollerService` помечен `@Profile("!bench")` и не поднимается в этом режиме. Это даёт воспроизводимый локальный прогон нагрузочных тестов (Apache Bench и аналоги) против чистого REST/JPA-слоя без побочных эффектов от Kafka-консьюмеров и фоновых джоб — изолирует то, что измеряется.

## API-эндпоинты

| Метод | Путь | Контроллер | Описание |
|---|---|---|---|
| GET | `/api/trains` | `TrainController` | Список всех тренировок |
| GET | `/api/trains/{id}` | `TrainController` | Тренировка по ID |
| POST | `/api/trains` | `TrainController` | Создать тренировку, публикует событие `TRAIN_EVENT` (`CREATED`) в outbox |
| DELETE | `/api/trains/{id}` | `TrainController` | Удалить тренировку, публикует событие `TRAIN_EVENT` (`DELETED`) в outbox |
| GET | `/api/v1/calculator/1rm?weight=&reps=` | `CalculatorController` | Расчёт одноповторного максимума по формуле Эпли |
| POST | `/api/progress/log` | `ProgressController` | Залогировать подход (повторы, вес) для существующей тренировки |
| GET | `/api/v1/stats/summary` | `StatsController` | Сводная статистика: число подходов и суммарный тоннаж |

Документация OpenAPI/Swagger — `/swagger-ui.html` (springdoc), спецификация — `/api-docs`.

### Kafka-топики

| Топик | Направление | Назначение |
|---|---|---|
| `saga-trains-command` | consume | Команды от Saga-Orchestrator (создание/откат личного кабинета) |
| `saga-trains-response` | produce (через Outbox) | Ответы оркестратору |
| `train-events` | produce (через Outbox) | События жизненного цикла тренировки (`CREATED`/`DELETED`) |
| `trains-events` | consume (не активен) | Заготовка под consumer, бин закомментирован |

## Технологический стек

| Категория | Технологии |
|---|---|
| Язык / платформа | Java 21 (Virtual Threads), Spring Boot 3.4.2 |
| Данные | PostgreSQL + Flyway, Redis, H2 (для `bench`-профиля и тестов) |
| Messaging | Apache Kafka, Spring Kafka, Transactional Outbox с claim-based polling |
| API | Spring Web, springdoc-openapi (Swagger UI) |
| Observability | Spring Boot Actuator, Micrometer + Prometheus registry, готовый docker-compose стек Prometheus + Grafana |
| Тестирование | JUnit 5, Mockito, Spring Kafka Test, H2 — 26 тестовых классов, включая отдельные unit- и integration-тесты для Outbox-пайплайна |
| Качество кода | JaCoCo с порогом покрытия строк 80%, привязанным к фазе `verify` (блокирует сборку при недоборе) |
| CI/CD | GitHub Actions: сборка и unit-тесты → integration-тесты (с Postgres/Kafka) → сборка и публикация Docker-образа в GHCR → раздельный деплой в staging (автоматически на push в `main`) и production (вручную через `workflow_dispatch`) |
| Контейнеризация | Docker (multi-stage build, `eclipse-temurin:21-jre`, non-root пользователь) |
| Деплой | Kubernetes + Kustomize (`k8s/`): Namespace, ConfigMap, Deployment, Service, HPA (1–6 реплик по CPU 70%) |

## Локальный запуск

### Зависимости

JDK 21+, Maven, PostgreSQL, Kafka, Redis (опционально).

### Переменные окружения

```bash
DB_HOST=localhost
DB_PORT=5444
DB_NAME=trains_db
DB_USERNAME=trains_user
DB_PASSWORD=secret
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost
REDIS_PORT=6379
SERVER_PORT=8035
```

### Сборка и тесты

```bash
./mvnw clean verify
```

### Запуск

```bash
./mvnw spring-boot:run
```

Сервис поднимется на `localhost:8035`. Health/Prometheus — `/actuator/health`, `/actuator/prometheus`.

### Нагрузочное тестирование локально (без внешних зависимостей)

```bash
./mvnw -Pbench spring-boot:run -Dspring-boot.run.profiles=bench
```

### Локальный мониторинг

```bash
docker compose -f monitoring/docker-compose.monitoring.yml up -d
```

Prometheus — `localhost:9090`, Grafana — `localhost:3000` (admin/admin по умолчанию).

## Связанные репозитории

- [Saga-Orchestrator](https://github.com/Maru3022/Saga-Orchestrator) — оркестратор саги создания пользователя, источник команд `saga-trains-command`
- [Training_Notification](https://github.com/Maru3022/Training_Notification) — соседний шаг саги, доставка уведомлений
- [Training-Nutrition](https://github.com/Maru3022/Training-Nutrition) — соседний шаг саги, расчёт питания
- [Eureka-server](https://github.com/Maru3022/Eureka-server) — service discovery для всей платформы
