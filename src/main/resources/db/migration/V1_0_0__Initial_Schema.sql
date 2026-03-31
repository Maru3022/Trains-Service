-- V1_0_0__Initial_Schema.sql

-- Таблица поездов
CREATE TABLE IF NOT EXISTS train (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    duration_minutes INTEGER
);

-- Таблица прогресса
CREATE TABLE IF NOT EXISTS progress (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL,
    reps INTEGER,
    weight DOUBLE PRECISION,
    date DATE,
    FOREIGN KEY (train_id) REFERENCES train(id) ON DELETE CASCADE
);

-- Таблица маршрутов
CREATE TABLE IF NOT EXISTS route (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    distance DOUBLE PRECISION
);

-- Таблица статистики
CREATE TABLE IF NOT EXISTS stats (
    id BIGSERIAL PRIMARY KEY,
    total_workouts INTEGER,
    average_weight DOUBLE PRECISION
);

-- Таблица Outbox для надежной доставки событий
CREATE TABLE IF NOT EXISTS outbox (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    key VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- Индекс для быстрого поиска необработанных событий
CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox(status);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox(created_at);
