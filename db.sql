-- Создание главной таблицы customers
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    registration_date DATE DEFAULT CURRENT_DATE
);

-- Создание связанной таблицы orders
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(20) DEFAULT 'pending',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Добавление индексов для оптимизации
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);

-- Добавление клиентов
INSERT INTO customers (first_name, last_name, email, phone) VALUES
    ('Иван', 'Петров', 'ivan.petrov@email.com', '+7-901-123-45-67'),
    ('Мария', 'Иванова', 'maria.ivanova@email.com', '+7-902-234-56-78'),
    ('Алексей', 'Сидоров', 'alex.sidorov@email.com', '+7-903-345-67-89'),
    ('Елена', 'Козлова', 'elena.kozlova@email.com', '+7-904-456-78-90');

-- Добавление заказов
INSERT INTO orders (customer_id, total_amount, status) VALUES
    (1, 1500.50, 'completed'),
    (1, 2300.00, 'pending'),
    (2, 850.75, 'shipped'),
    (3, 3200.00, 'pending'),
    (2, 1200.25, 'completed'),
    (4, 950.00, 'processing');

-- Проверка структуры таблиц
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'customers';

SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'orders';

-- Проверка связей и данных
SELECT 
    c.first_name,
    c.last_name,
    c.email,
    o.order_id,
    o.total_amount,
    o.status,
    o.order_date
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
ORDER BY c.last_name, o.order_date;