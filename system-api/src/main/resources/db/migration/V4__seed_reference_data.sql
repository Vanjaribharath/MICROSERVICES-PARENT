-- Day 7 — Flyway V4: Seed data for development
INSERT INTO categories (name, description) VALUES
    ('Electronics',  'Electronic devices and accessories'),
    ('Clothing',     'Apparel and fashion items'),
    ('Books',        'Educational and entertainment books'),
    ('Food',         'Consumable food and beverage products'),
    ('Hardware',     'Industrial and construction materials');

INSERT INTO suppliers (name, contact_email) VALUES
    ('TechSupply Co.', 'orders@techsupply.com'),
    ('Fashion Direct', 'supply@fashiondirect.in'),
    ('BookWorld',      'orders@bookworld.in');
