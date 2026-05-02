-- Day 23 — Domain Events table (Kafka event audit log)
-- Every event produced to/consumed from Kafka is recorded here
CREATE TABLE domain_events (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        UUID            NOT NULL DEFAULT gen_random_uuid(),
    event_type      VARCHAR(100)    NOT NULL,  -- e.g. ProductCreated, ProductUpdated
    topic           VARCHAR(100)    NOT NULL,  -- Kafka topic name
    aggregate_id    VARCHAR(100)    NOT NULL,  -- product ID, order ID etc.
    aggregate_type  VARCHAR(50)     NOT NULL,  -- Product, Order etc.
    payload         JSONB           NOT NULL,  -- full event payload
    status          VARCHAR(20)     NOT NULL DEFAULT 'PRODUCED',  -- PRODUCED / CONSUMED / FAILED
    correlation_id  VARCHAR(64),
    service_name    VARCHAR(100)    NOT NULL,
    produced_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    consumed_at     TIMESTAMP,
    retry_count     INT             NOT NULL DEFAULT 0,
    error_message   TEXT
);

CREATE INDEX idx_events_event_id        ON domain_events(event_id);
CREATE INDEX idx_events_event_type      ON domain_events(event_type);
CREATE INDEX idx_events_aggregate_id    ON domain_events(aggregate_id);
CREATE INDEX idx_events_status          ON domain_events(status);
CREATE INDEX idx_events_correlation_id  ON domain_events(correlation_id);
CREATE INDEX idx_events_produced_at     ON domain_events(produced_at DESC);

COMMENT ON TABLE domain_events IS 'Day 23 — Kafka event audit trail. Every produced/consumed event logged here.';
COMMENT ON COLUMN domain_events.payload IS 'Full JSON payload of the event';
COMMENT ON COLUMN domain_events.status IS 'PRODUCED=sent to Kafka, CONSUMED=received by consumer, FAILED=error';
