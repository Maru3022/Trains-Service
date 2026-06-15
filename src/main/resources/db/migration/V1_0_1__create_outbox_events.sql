-- V1_0_1__create_outbox_events.sql

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100),
    aggregate_id VARCHAR(100),
    event_type VARCHAR(100),
    topic VARCHAR(255) NOT NULL,
    "key" VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    error_message TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status_created_at ON outbox_events(status, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);
