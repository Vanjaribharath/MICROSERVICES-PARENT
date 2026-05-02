-- DB Logging requirement — application_logs table
CREATE TABLE application_logs (
    id               BIGSERIAL     PRIMARY KEY,
    timestamp        TIMESTAMP     NOT NULL DEFAULT NOW(),
    level            VARCHAR(10)   NOT NULL,
    service_name     VARCHAR(100)  NOT NULL,
    logger_name      VARCHAR(255),
    message          TEXT          NOT NULL,
    correlation_id   VARCHAR(64),
    trace_id         VARCHAR(64),
    span_id          VARCHAR(64),
    thread_name      VARCHAR(100),
    exception_detail TEXT,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_logs_correlation_id ON application_logs(correlation_id);
CREATE INDEX idx_logs_timestamp      ON application_logs(timestamp DESC);
CREATE INDEX idx_logs_level          ON application_logs(level);
CREATE INDEX idx_logs_service        ON application_logs(service_name);
COMMENT ON TABLE application_logs IS 'Structured log storage with correlation ID tracking';
COMMENT ON COLUMN application_logs.correlation_id IS 'X-Correlation-ID for request tracing across services';
