-- Day 7 — Flyway V3: Products table with FK constraints
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200)    NOT NULL,
    price       NUMERIC(12,2)   NOT NULL CHECK (price >= 0),
    status      VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    category_id BIGINT          NOT NULL REFERENCES categories(id),
    supplier_id BIGINT          REFERENCES suppliers(id),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status   ON products(status);
CREATE INDEX idx_products_name     ON products(name);
