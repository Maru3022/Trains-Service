# Архитектура Trains Service

## Диаграмма компонентов

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Контроллеры                     │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐ │
│  │TrainController│ │ProgressCtrl  │  │CalculatorCtrl     │ │
│  └──────┬──────┘  └──────┬───────┘  └────────┬───────────┘ │
└────────┼──────────────────┼────────────────────┼─────────────┘
         │                  │                    │
         ▼                  ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                  Business Logic Services                    │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────────┐   │
│  │ TrainService │  │MovementSvc  │  │CalculatorService│   │
│  └──────┬───────┘  └──────┬──────┘  └──────────────────┘   │
└────────┼────────────────┼──────────────────────────────────┘
         │                │
         │       ┌────────┴─────────┐
         │       │                  │
         ▼       ▼                  ▼
    ┌────────────────────────────────────────┐
    │     Messaging & Event Layer            │
    │  ┌──────────────────────────────────┐  │
    │  │  TrainEventProducer              │  │
    │  │  (Saves to Outbox)               │  │
    │  └──────────────────────────────────┘  │
    │              ▼                          │
    │  ┌──────────────────────────────────┐  │
    │  │  OutboxProcessor (Scheduled)     │  │
    │  │  (Sends to Kafka)                │  │
    │  └──────────────────────────────────┘  │
    └────────────────┬───────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
    ┌──────────────┐      ┌──────────────┐
    │  PostgreSQL  │      │  Kafka       │
    │  (Outbox DB) │      │  (Events)    │
    └──────────────┘      └──────────────┘
```

## Слои архитектуры

### 1. **Presentation Layer (REST Controllers)**
- **TrainController**: REST API для операций с поездами (CRUD)
- **ProgressController**: Логирование прогресса тренировок
- **CalculatorController**: Расчеты (например, 1RM)
- **StatsController**: Получение статистики

### 2. **Business Logic Layer (Services)**
- **TrainService**: Управление поездами + публикация событий
- **MovementService**: Логирование прогресса
- **CalculatorService**: Вычисления
- **StatisticsService**: Агрегация статистики

### 3. **Event & Messaging Layer**
- **TrainEventProducer**: Сохраняет события в таблицу `outbox`
- **OutboxProcessor**: Асинхронно обрабатывает события и отправляет в Kafka
- **OutboxEventRepository**: Доступ к таблице outbox
- **KafkaConfig**: Конфигурация Kafka topics

### 4. **Data Access Layer (Repositories)**
- **TrainRepository**: JPA для Train
- **ProgressRepository**: JPA для Progress
- **OutboxEventRepository**: JPA для OutboxEvent
- **StatsRepository**: JPA для Stats

### 5. **Database Layer**
- **PostgreSQL**: Основная БД для всех данных + таблица outbox
- **H2** (тесты): В памяти БД для unit тестов

## Outbox Pattern - Детальное описание

### Проблема без Outbox Pattern
```
Transaction Start
  │
  ├─ Save Train to DB ✅
  │
  ├─ Send Event to Kafka ❌ (Network fails)
  │
Transaction Commit
└─ Data saved but Event lost
```

### Решение с Outbox Pattern
```
Transaction Start
  │
  ├─ Save Train to DB ✅
  │
  ├─ Save Event to Outbox Table ✅ (same transaction)
  │
Transaction Commit ✅
  │
Scheduled Job (5 sec interval)
  │
  ├─ Find Pending Events
  │
  ├─ Send to Kafka ✅
  │
  ├─ Update Status to SENT ✅
  │
  └─ If fails: Status = FAILED for retry
```

### Состояния события

```
PENDING
  │
  ├─ [OutboxProcessor runs]
  │
  ├─ Send to Kafka Success
  │  │
  │  └─► SENT ✅
  │
  └─ Send to Kafka Failed
     │
     └─► FAILED ⚠️ (manual intervention needed)
```

## Жизненный цикл события

### 1. Создание события
```java
// TrainService.saveTrain()
Train savedTrain = trainRepository.save(train); // DB Transaction
TrainEventDTO event = new TrainEventDTO(savedTrain.getId(), "CREATED");
trainEventProducer.sendEvent(event); // Saves to outbox (same transaction)
```

### 2. Сохранение в Outbox
```java
// TrainEventProducer.sendEvent()
@Transactional
public void sendEvent(TrainEventDTO event) {
    String payload = objectMapper.writeValueAsString(event);
    OutboxEvent outboxEvent = new OutboxEvent();
    outboxEvent.setTopic("train-events");
    outboxEvent.setKey(event.getTrainId());
    outboxEvent.setPayload(payload);
    outboxEventRepository.save(outboxEvent); // PENDING status by default
}
```

### 3. Асинхронная обработка
```java
// OutboxProcessor.processOutbox() - runs every 5 seconds
@Scheduled(fixedDelay = 5000)
@Transactional
public void processOutbox() {
    List<OutboxEvent> pendingEvents = 
        outboxEventRepository.findByStatusOrderByCreatedAt(PENDING);
    
    for (OutboxEvent event : pendingEvents) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload());
            event.setStatus(SENT);
            outboxEventRepository.save(event);
        } catch (Exception e) {
            event.setStatus(FAILED);
            outboxEventRepository.save(event);
        }
    }
}
```

## Поток данных

### Создание поезда с событием

```
1. Client
   │
   └─► POST /api/trains
       │
       ▼
2. TrainController.createTrain()
   │
   ├─► TrainService.saveTrain()
   │   │
   │   ├─► trainRepository.save(train) [DB]
   │   │
   │   └─► trainEventProducer.sendEvent(event)
   │       │
   │       ├─► objectMapper.writeValueAsString(event)
   │       │
   │       └─► outboxEventRepository.save(outboxEvent) [DB]
   │
   └─► HTTP 200 Response
       │
3. OutboxProcessor (Scheduled, runs every 5 sec)
   │
   ├─► SELECT * FROM outbox WHERE status = 'PENDING'
   │
   ├─► FOR EACH pending event:
   │   ├─► kafkaTemplate.send(topic, key, payload)
   │   └─► UPDATE outbox SET status = 'SENT'
   │
   └─► Kafka Topic: "train-events"
       │
       └─► TrainEventListener (if enabled)
           └─► Process event asynchronously
```

## Конфигурация и свойства

### application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
  jpa:
    hibernate:
      ddl-auto: update  # or validate in production
  datasource:
    url: jdbc:postgresql://localhost:5444/trains_db
```

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # In-memory H2 for tests
    driver-class-name: org.h2.Driver
  task:
    scheduling:
      enabled: false  # Disable OutboxProcessor in tests
```

## Тестирование

### Unit Tests
- **CalculatorServiceTest**: Бизнес-логика без зависимостей
- **MovementServiceTest**: Mockito для тестирования

### Integration Tests
- **TrainMessagingTest**: Проверка сохранения в outbox
- **TrainServiceIntegrationTest**: Full flow с БД
- **MovementServiceIntegrationTest**: Обработка ошибок

### Test Coverage
- Jacoco интегрирован в pom.xml
- Результаты: `target/site/jacoco/index.html`

## Мониторинг и логирование

### Логирование
```java
@Slf4j
public class OutboxProcessor {
    public void processOutbox() {
        log.info("Processing outbox events...");
        log.debug("Pending events: {}", pendingEvents.size());
        log.error("Failed to send event {}: {}", eventId, exception);
    }
}
```

### Метрики (Prometheus)
- HTTP запросы: `http_server_requests_seconds`
- Kafka отправки: `kafka_producer_record_send_total`
- Размер очереди БД: Custom metric

## Обработка ошибок

### Retry Strategy
1. **Автоматический retry**: OutboxProcessor повторяет каждые 5 секунд
2. **Состояние FAILED**: Требует ручного вмешательства
3. **Логирование**: Все ошибки логируются для анализа

### Обработка исключений
```java
try {
    kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload());
    event.setStatus(SENT);
} catch (Exception e) {
    log.error("Failed to send event {}: {}", event.getId(), e.getMessage());
    event.setStatus(FAILED);
}
outboxEventRepository.save(event);
```

## Best Practices

1. **Транзакционность**: Используйте `@Transactional` для атомарности
2. **Индексы**: Создайте индекс на колонке `status` для быстрого поиска
3. **Мониторинг**: Отслеживайте события со статусом FAILED
4. **Retry Logic**: Настройте временные интервалы в зависимости от нагрузки
5. **Чистка**: Периодически удаляйте обработанные события
