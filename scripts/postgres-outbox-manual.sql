-- Вариант 2 (ручное создание outbox при ddl-auto: validate в проде).
-- ВАЖНО: этот проект использует таблицу outbox под сущность OutboxEvent (Kafka topic + payload),
-- а НЕ шаблон с UUID/aggregate_id из общих гайдов — иначе Java-код не совпадёт со схемой.
--
-- Подключение к PostgreSQL (имя контейнера и пользователь — как в вашем общем
-- docker-compose, например в C:\Project; подставьте свои значения):
--   docker exec -it <postgres-container> psql -U <user> -d <database>
--
-- Затем выполните блок ниже (или \i путь/к/файлу).

CREATE TABLE IF NOT EXISTS outbox (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    "key" VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox(status);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox(created_at);
