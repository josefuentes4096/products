CREATE TABLE IF NOT EXISTS product (
    id          SERIAL          PRIMARY KEY,
    nombre      VARCHAR(255)    NOT NULL,
    descripcion TEXT            NOT NULL,
    precio      NUMERIC(19, 2)  NOT NULL,
    categoria   VARCHAR(100),
    imagen_url  VARCHAR(500),
    stock       INTEGER         NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id          SERIAL          PRIMARY KEY,
    usuario_id  INTEGER         NOT NULL,
    estado      VARCHAR(20)     NOT NULL,
    total       NUMERIC(19, 2)  NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    id         SERIAL          PRIMARY KEY,
    order_id   INTEGER         NOT NULL,
    product_id INTEGER         NOT NULL,
    cantidad   INTEGER         NOT NULL,
    unit_price NUMERIC(19, 2)  NOT NULL,
    subtotal   NUMERIC(19, 2)  NOT NULL,
    CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS settings (
    id            SERIAL       PRIMARY KEY,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    CONSTRAINT uq_settings_key UNIQUE (setting_key)
);
