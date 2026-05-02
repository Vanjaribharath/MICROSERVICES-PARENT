-- Day 24 — Error Events table
-- Captures all system errors with full context for debugging and alerting

CREATE TABLE error_events (
    id              BIGSERIAL       PRIMARY KEY,
    error_id        UUID            NOT NULL DEFAULT gen_random_uuid(),
    error_type      VARCHAR(100)    NOT NULL,  -- exception class name
    error_code      VARCHAR(50),               -- HTTP status or custom code
    message         TEXT            NOT NULL,
    stack_trace     TEXT,
    service_name    VARCHAR(100)    NOT NULL,
    endpoint        VARCHAR(255),              -- API endpoint that caused the error
    http_method     VARCHAR(10),
    correlation_id  VARCHAR(64),
    saga_id         UUID,                      -- if error occurred during a Saga
    user_id         VARCHAR(100),
    request_payload TEXT,
    occurred_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    resolved        BOOLEAN         NOT NULL DEFAULT FALSE,
    resolved_at     TIMESTAMP,
    resolution_note TEXT
);

CREATE INDEX idx_errors_error_id      ON error_events(error_id);
CREATE INDEX idx_errors_type          ON error_events(error_type);
CREATE INDEX idx_errors_service       ON error_events(service_name);
CREATE INDEX idx_errors_correlation   ON error_events(correlation_id);
CREATE INDEX idx_errors_occurred_at   ON error_events(occurred_at DESC);
CREATE INDEX idx_errors_resolved      ON error_events(resolved);

COMMENT ON TABLE error_events IS 'Day 24 — Centralized error registry. All errors from all services persisted here.';
