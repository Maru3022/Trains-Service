# 🚄 Trains-Service

**Trains-Service** — микросервис на Java Spring Boot: REST API для сущности «поезд / тренировка», учёт прогресса, статистика, калькулятор 1RM, **Transactional Outbox** и Kafka, готовность к **Kubernetes** (манифесты Kustomize в `k8s/`), CI/CD (сборка, тесты, OWASP, образ в GHCR, валидация манифестов, опциональный деплой в кластер и SSH-стейджинг/прод).

Инфраструктура БД/брокеров обычно поднимается **общим** compose родительского репозитория; в Kubernetes — через ваши сервисы и `ConfigMap`/`Secret` (см. ниже).

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
| **Сообщения** | Apache Kafka (`SPRING_KAFKA_BOOTSTRAP_SERVERS`, по умолчанию `localhost:9092`) |
| **Документация** | Swagger UI (OpenAPI 2.8.4) |
| **Мониторинг** | Prometheus + Grafana |
| **Оркестрация** | Kubernetes (база в `k8s/`, Kustomize) |
| **Инструменты** | Lombok, Maven |

---

## ☸️ Kubernetes

В каталоге **`k8s/`** лежит **Kustomize-база**: `Namespace`, `ConfigMap` (несекретные переменные окружения), `Deployment` (пробы `/actuator/health/liveness` и `readiness`, ресурсы, rolling update), `Service` (порт **8035**), **HPA** по CPU (нужен Metrics Server в кластере).

### Локальная проверка (без кластера)

```bash
kubectl kustomize k8s/
kubectl kustomize k8s/ | kubectl apply --dry-run=client -f -
```

### Установка в кластер

1. Подставьте в `k8s/configmap.yaml` реальные DNS сервисов PostgreSQL, Kafka и Redis (или используйте **overlay** Kustomize на команду/окружение).
2. Создайте секрет с паролем БД (пример в `k8s/secret.yaml.example`):
   ```bash
   kubectl -n trains-service create secret generic trains-service-secret \
     --from-literal=DB_PASSWORD='***'
   ```
3. Образ по умолчанию: `ghcr.io/maru3022/trains-service:latest` (в `kustomization.yaml` → `images`). Для приватного GHCR добавьте `imagePullSecrets` в `Deployment` (patch) и `docker login` в registry кластера.
4. Применение:
   ```bash
   kubectl apply -k k8s/
   ```

Остальные микросервисы вы можете оформлять так же отдельными каталогами/overlays и связывать через общий Ingress или service mesh — этот репозиторий даёт **шаблон для одного сервиса**.

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
* **Инфраструктура (PostgreSQL, Kafka, Redis)** поднимается **общим** `docker-compose` в родительском каталоге (например `C:\Project`), куда входят все микросервисы. В этом репозитории отдельного compose для приложения нет — укажите хосты и порты через переменные окружения (`DB_HOST`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `REDIS_HOST` и т.д.), согласованные с вашим compose.
* По умолчанию в `application.yml`: PostgreSQL `localhost:5444`, Kafka `localhost:9092`, Redis `localhost:6379`.

### 2. OpenAPI / Swagger UI
После запуска сервиса: [http://localhost:8035/swagger-ui/index.html](http://localhost:8035/swagger-ui/index.html)

**Нагрузочный прогон без Docker (H2 в памяти, без Kafka):**  
`mvnw -Pbench spring-boot:run "-Dspring-boot.run.profiles=bench"` — затем, например, `ab -n 5000 -c 50 http://127.0.0.1:8035/api/trains`.

### 3. Сборка проекта
В корневой папке выполните:

```bash
mvn clean install
```

### 4. Запуск приложения

```bash
mvn spring-boot:run
```

Для production включите профиль `prod` (`spring.profiles.active=prod`): в `application-prod.yml` задано `ddl-auto: validate` (схема не меняется Hibernate автоматически).

---

### Ручное создание таблицы `outbox` (Transactional Outbox)

Если в production используете `ddl-auto: validate` и таблицы ещё нет, выполните SQL из `scripts/postgres-outbox-manual.sql` в своей БД (имя контейнера/psql — как в вашем общем compose).

---

## 🔄 CI/CD (GitHub Actions)

Рабочий процесс `.github/workflows/main.yml` включает:

1. **Сборка и тесты** — JDK 21, `mvn test`, публикация результатов тестов, JaCoCo, артефакт JAR.
2. **Качество** — OWASP Dependency-Check (не валит пайплайн при сбое NVD).
3. **Kubernetes** — `kubectl kustomize` + `kubectl apply --dry-run=client`, затем **kubeconform** против схемы API 1.30.
4. **Docker** — сборка и push в **GHCR** (после успеха п.1–3), **Trivy** по образу.
5. **Деплой** — по желанию: SSH staging/develop, SSH production/main, **Kubernetes** на `main` при наличии секрета **`KUBE_CONFIG_B64`** (kubeconfig в base64): `kubectl apply -k k8s/` и `kubectl set image` на `:latest`.
6. **Уведомления** — Slack при настроенном webhook.

---

## 📁 Структура проекта

```
Trains-Service/
├── src/
│   └── main/java/...
├── k8s/
│   ├── kustomization.yaml
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── hpa.yaml
│   └── secret.yaml.example
├── scripts/
│   └── postgres-outbox-manual.sql
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
