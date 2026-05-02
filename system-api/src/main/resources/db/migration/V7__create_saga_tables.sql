-- Day 24 — Saga Pattern tables
-- Tracks long-running distributed transactions across services

CREATE TABLE saga_transactions (
    id              BIGSERIAL       PRIMARY KEY,
    saga_id         UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    saga_type       VARCHAR(100)    NOT NULL,  -- e.g. CreateProductSaga
    status          VARCHAR(30)     NOT NULL DEFAULT 'STARTED',
    -- Status: STARTED / STEP_1_DONE / STEP_2_DONE / COMPLETED / COMPENSATING / COMPENSATED / FAILED
    current_step    VARCHAR(100),
    payload         JSONB           NOT NULL,  -- initial saga input
    result          JSONB,                     -- final result after completion
    correlation_id  VARCHAR(64),
    service_name    VARCHAR(100)    NOT NULL,
    started_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    error_message   TEXT
);

CREATE TABLE saga_steps (
    id              BIGSERIAL       PRIMARY KEY,
    saga_id         UUID            NOT NULL REFERENCES saga_transactions(saga_id),
    step_name       VARCHAR(100)    NOT NULL,
    step_order      INT             NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- Status: PENDING / EXECUTING / COMPLETED / FAILED / COMPENSATING / COMPENSATED
    input_payload   JSONB,
    output_payload  JSONB,
    compensate_payload JSONB,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    error_message   TEXT
);

CREATE INDEX idx_saga_saga_id       ON saga_transactions(saga_id);
CREATE INDEX idx_saga_status        ON saga_transactions(status);
CREATE INDEX idx_saga_type          ON saga_transactions(saga_type);
CREATE INDEX idx_saga_correlation   ON saga_transactions(correlation_id);
CREATE INDEX idx_saga_steps_saga    ON saga_steps(saga_id);
CREATE INDEX idx_saga_steps_status  ON saga_steps(status);

COMMENT ON TABLE saga_transactions IS 'Day 24 — Saga orchestration state machine. Tracks all long-running distributed transactions.';
COMMENT ON TABLE saga_steps        IS 'Day 24 — Individual steps within a Saga. Each step has forward + compensate action.';
