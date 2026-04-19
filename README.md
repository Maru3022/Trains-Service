# 🚄 Trains-Service

**Trains-Service** — это бэкенд-система на Java Spring Boot для управления данными о поездах (или тренировках) и мониторинга прогресса в реальном времени. Проект сочетает в себе классический REST API и современную событийную архитектуру на базе Apache Kafka.

---

## 🌟 Основные возможности

### 🛠 Управление данными (CRUD)
Полный цикл управления сущностью `Train`:
* **Создание**: Добавление новых записей через POST-запросы.
* **Просмотр**: Получение полного списка или поиск конкретного объекта по ID.
* **Удаление**: Очистка базы данных от неактуальных записей.

### 📈 Мониторинг прогресса
Специализированный сервис `MovementService` для фиксации активности:
* **Регистрация подходов**: Сохранение количества повторений и веса для конкретной записи.
* **История**: Каждая запись прогресса автоматически привязывается к дате и времени через `ProgressRepository`.

### 📡 События Kafka
Интеграция с Kafka позволяет системе реагировать на изменения асинхронно:
* **Outbox Storage**: События сохраняются в таблице `outbox`.
* **Async Sender**: `OutboxProcessor` отправляет события в топик `train-events` каждые 5 секунд.
* **Reliable Delivery**: Гарантированная доставка через Outbox Pattern.

---

## 🏗 Технологический стек

| Компонент | Технология |
| :--- | :--- |
| **Ядро** | Java 21 & Spring Boot 3.4.2 |
| **База данных** | PostgreSQL (порт `5444`) |
| **Кэширование** | Redis |
| **Сообщения** | Apache Kafka (порт `9095`) |
| **Документация** | Swagger UI (OpenAPI 2.8.4) |
| **Мониторинг** | Prometheus + Grafana |
| **Инструменты** | Lombok, Maven |

---

## 🚀 API Эндпоинты

### Управление поездами (`TrainController`)
| Метод | Путь | Описание |
| :--- | :--- | :--- |
| `GET` | `/api/trains` | Получить список всех поездов |
| `GET` | `/api/trains/{id}` | Получить информацию по ID |
| `POST` | `/api/trains` | Создать новую запись |
| `DELETE` | `/api/trains/{id}` | Удалить запись по ID |

### Статистика (`StatsController`)
| Метод | Путь | Описание |
| :--- | :--- | :--- |
| `GET` | `/api/v1/stats/summary` | Получить сводку по тренировкам |

---

## 📊 Мониторинг (Prometheus + Grafana)

Проект включает полноценный стек мониторинга, разворачиваемый через общий docker-compose файл микросервисной архитектуры.

### Архитектура мониторинга

```
┌─────────────────┐     scrape      ┌────────────────┐     query      ┌─────────────┐
│  trains-app     │ ◄────────────── │   Prometheus   │ ◄──────────── │   Grafana   │
│  :8035/actuator │                 │   :9090        │                │   :3000     │
├─────────────────┤                 └────────────────┘                └─────────────┘
│  api-gateway    │ ◄──────────────
│  :8075/actuator │
└─────────────────┘
```

### Структура файлов мониторинга

```
monitoring/
├── prometheus/
│   └── prometheus.yml                    # Конфигурация сбора метрик
└── grafana/
    └── provisioning/
        ├── datasources/
        │   └── datasource.yml            # Авто-подключение Prometheus
        └── dashboards/
            └── dashboards.yml            # Авто-загрузка дашбордов
```

### Конфигурация Prometheus (`prometheus.yml`)

Prometheus собирает метрики с двух сервисов каждые **15 секунд** через Spring Boot Actuator:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8075']

  - job_name: 'trains-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['trains-app:8035']
```

> **Важно**: Для работы эндпоинта `/actuator/prometheus` в `application.yml` должно быть включено:
> ```yaml
> management:
>   endpoints:
>     web:
>       exposure:
>         include: prometheus, health, info, metrics
>   metrics:
>     export:
>       prometheus:
>         enabled: true
> ```

### Конфигурация Grafana

Grafana настраивается **автоматически** при старте через механизм provisioning — вручную ничего добавлять не нужно.

**Datasource** (`datasource.yml`): автоматически подключает Prometheus как источник данных по умолчанию.

**Dashboards** (`dashboards.yml`): Grafana читает JSON-файлы дашбордов из директории `/var/lib/grafana/dashboards`.

Чтобы добавить собственный дашборд:
1. Создайте дашборд в Grafana UI.
2. Экспортируйте его: `Dashboard → Share → Export → Save to file`.
3. Поместите JSON-файл в `monitoring/grafana/provisioning/dashboards/`.
4. Перезапустите контейнер Grafana — дашборд появится автоматически.

### Доступ к интерфейсам

| Сервис | URL | Логин / Пароль |
| :--- | :--- | :--- |
| **Grafana** | http://localhost:3000 | `admin` / `admin` |
| **Prometheus** | http://localhost:9090 | — |
| **Actuator (trains)** | http://localhost:8035/actuator | — |
| **Actuator (gateway)** | http://localhost:8075/actuator | — |

### Ключевые метрики для мониторинга

| Метрика | Описание |
| :--- | :--- |
| `http_server_requests_seconds` | Латентность HTTP-запросов по эндпоинтам |
| `jvm_memory_used_bytes` | Потребление памяти JVM |
| `hikaricp_connections_active` | Активные соединения с БД (connection pool) |
| `kafka_producer_record_send_total` | Количество отправленных сообщений в Kafka |
| `process_cpu_usage` | Загрузка CPU приложением |

---

## 🔧 Настройка и запуск

### 1. Предварительные требования
* Java 21
* Maven 3.8+
* **PostgreSQL**: база данных `trains_db` на порту `5444`.
* **Kafka**: брокер доступен на `localhost:9095`.

### 2. Сборка проекта
В корневой папке выполните:

```bash
mvn clean install
```

### 3. Запуск приложения

```bash
mvn spring-boot:run
```

---

## 📁 Структура проекта

```
Trains-Service/
├── src/
│   └── main/java/...
├── monitoring/
│   ├── prometheus/
│   │   └── prometheus.yml
│   └── grafana/
│       └── provisioning/
│           ├── datasources/datasource.yml
│           └── dashboards/dashboards.yml
├── .github/
│   └── workflows/
│       └── main.yml
├── Dockerfile
└── pom.xml
```
