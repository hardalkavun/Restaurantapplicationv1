-- Referans MySQL şeması (JPA entity'leriyle uyumlu).
-- Uygulama genelde spring.jpa.hibernate.ddl-auto=update ile tabloları kendisi oluşturur/günceller;
-- bu dosya manuel kurulum veya dokümantasyon içindir.

CREATE DATABASE IF NOT EXISTS restaurant_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_management;

-- Giriş kullanıcıları (MANAGER / COURIER); şifre BCrypt hash olarak tutulur
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
);

-- Ürün grupları
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
);

-- Menü kalemi; görsel veritabanında LONGBLOB
CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL,
    active BIT NOT NULL,
    image_name VARCHAR(255),
    image_type VARCHAR(255),
    image_data LONGBLOB,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Müşteri siparişi; isteğe bağlı atanmış kurye (users FK)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_name VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    address VARCHAR(500) NOT NULL,
    order_date_time DATETIME(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    assigned_courier_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_assigned_courier
        FOREIGN KEY (assigned_courier_id) REFERENCES users (id)
);

-- Sipariş ile ürün arasında çoka çok ilişki (JPA @JoinTable order_products)
CREATE TABLE IF NOT EXISTS order_products (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_products_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_products_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);
