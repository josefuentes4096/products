-- Setup completo: schema + datos de ejemplo (MySQL / TiDB Serverless)
-- Guitarras electricas, pedales de efecto y amplificadores valvulares

-- -------------------------------------------------------------------------
-- Schema (idempotente; redundante si Flyway ya ejecuto V1)
-- -------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS settings (
    id            INT          NOT NULL AUTO_INCREMENT,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_settings_key UNIQUE (setting_key)
);

CREATE TABLE IF NOT EXISTS product (
    id          INT             NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(255)    NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10, 2)  NOT NULL,
    categoria   VARCHAR(100),
    imagen_url  VARCHAR(500),
    stock       INT             NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders (
    id          INT             NOT NULL AUTO_INCREMENT,
    usuario_id  INT             NOT NULL,
    estado      VARCHAR(50)     NOT NULL,
    total       DECIMAL(10, 2)  NOT NULL,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_item (
    id         INT            NOT NULL AUTO_INCREMENT,
    order_id   INT            NOT NULL,
    product_id INT            NOT NULL,
    cantidad   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal   DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orderitem_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES product (id)
);

-- -------------------------------------------------------------------------
-- Configuracion (idempotente: INSERT IGNORE evita choque con V2 de Flyway)
-- -------------------------------------------------------------------------

INSERT IGNORE INTO settings (setting_key, setting_value)
VALUES ('minimum_stock', '5');

-- -------------------------------------------------------------------------
-- Reset de productos (permite re-ejecutar el script sin duplicar filas)
-- FOREIGN_KEY_CHECKS=0 es necesario porque order_item tiene FK a product
-- -------------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE product;
SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------------------
-- Datos de ejemplo
-- -------------------------------------------------------------------------

INSERT INTO product (nombre, descripcion, precio, categoria, imagen_url, stock, created_at, updated_at) VALUES
-- Guitarras electricas
('Fender Stratocaster American Pro II',     'Guitarra electrica de cuerpo solido con pastillas V-Mod II y cuello en C moderno',            1500.00, 'Guitarras',      'https://placehold.co/400x300?text=Stratocaster',    8,  NOW(), NOW()),
('Gibson Les Paul Standard 60s',            'Guitarra electrica con cuerpo de caoba, tapa de arce flameado y pastillas Burstbucker',        2800.00, 'Guitarras',      'https://placehold.co/400x300?text=LesPaul',         5,  NOW(), NOW()),
('Fender Telecaster American Professional', 'Guitarra electrica de cuerpo de aliso con pastillas V-Mod II single-coil',                    1450.00, 'Guitarras',      'https://placehold.co/400x300?text=Telecaster',      6,  NOW(), NOW()),
('Gibson SG Standard',                      'Guitarra electrica de doble cutaway con cuerpo de caoba y pastillas 490R/490T',               1200.00, 'Guitarras',      'https://placehold.co/400x300?text=SGStandard',      4,  NOW(), NOW()),
('PRS SE Custom 24',                        'Guitarra electrica con tapa de arce y pastillas humbucker SE 85/15',                           850.00, 'Guitarras',      'https://placehold.co/400x300?text=PRSCustom24',     7,  NOW(), NOW()),

-- Pedales de efecto
('Ibanez Tube Screamer TS9',               'Pedal de overdrive clasico con circuito simetrico de recorte y control de tono',               120.00, 'Pedales',        'https://placehold.co/400x300?text=TubeScreamer',   15,  NOW(), NOW()),
('Boss DS-1 Distortion',                   'Pedal de distorsion compacto, el mas vendido en la historia con sonido agresivo y definido',    80.00, 'Pedales',        'https://placehold.co/400x300?text=BossDS1',        25,  NOW(), NOW()),
('Electro-Harmonix Big Muff Pi',           'Pedal de fuzz con sustain masivo, favorito del grunge y el rock alternativo',                  110.00, 'Pedales',        'https://placehold.co/400x300?text=BigMuff',        12,  NOW(), NOW()),
('TC Electronic Hall of Fame 2',           'Pedal de reverb con tecnologia TonePrint, shimmer y algoritmos de sala, plate y spring',       150.00, 'Pedales',        'https://placehold.co/400x300?text=HallOfFame',     10,  NOW(), NOW()),
('MXR Phase 90',                           'Pedal de phaser de cuatro etapas, sonido clasico usado por Eddie Van Halen',                    95.00, 'Pedales',        'https://placehold.co/400x300?text=Phase90',        18,  NOW(), NOW()),
('Boss DD-8 Digital Delay',                'Pedal de delay digital con 11 modos, looper de 40 segundos y tap tempo',                       180.00, 'Pedales',        'https://placehold.co/400x300?text=BossDD8',         9,  NOW(), NOW()),

-- Amplificadores valvulares
('Marshall DSL40CR',                       'Amplificador valvular de 40W con dos canales, reverb digital y potencia reducible a 20W',     1200.00, 'Amplificadores', 'https://placehold.co/400x300?text=MarshallDSL40',   3,  NOW(), NOW()),
('Vox AC30C2',                             'Amplificador valvular de 30W con dos canales, tremolo y reverb, icono del sonido britanico',  1800.00, 'Amplificadores', 'https://placehold.co/400x300?text=VoxAC30',         2,  NOW(), NOW()),
('Fender Blues Junior IV',                 'Amplificador valvular de 15W con reverb spring y control de tono FAT',                         700.00, 'Amplificadores', 'https://placehold.co/400x300?text=BluesJunior',     4,  NOW(), NOW()),
('Mesa Boogie Rectoverb 25',               'Amplificador valvular de 25W con dos canales y rectificador de alta ganancia',                2500.00, 'Amplificadores', 'https://placehold.co/400x300?text=MesaBoogie',      1,  NOW(), NOW());
