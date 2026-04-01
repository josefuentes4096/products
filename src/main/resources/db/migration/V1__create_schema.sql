CREATE TABLE IF NOT EXISTS product (
    id          INT             NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(255)    NOT NULL,
    descripcion TEXT            NOT NULL,
    precio      DECIMAL(19, 2)  NOT NULL,
    categoria   VARCHAR(100),
    imagen_url  VARCHAR(500),
    stock       INT             NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders (
    id          INT             NOT NULL AUTO_INCREMENT,
    usuario_id  INT             NOT NULL,
    estado      VARCHAR(20)     NOT NULL,
    total       DECIMAL(19, 2)  NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_item (
    id          INT             NOT NULL AUTO_INCREMENT,
    order_id    INT             NOT NULL,
    product_id  INT             NOT NULL,
    cantidad    INT             NOT NULL,
    unit_price  DECIMAL(19, 2)  NOT NULL,
    subtotal    DECIMAL(19, 2)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS settings (
    id            INT          NOT NULL AUTO_INCREMENT,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_settings_key UNIQUE (setting_key)
);
