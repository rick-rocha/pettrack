CREATE TABLE produtos (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    nome                VARCHAR(150)    NOT NULL,
    descricao           VARCHAR(500),
    codigo_sku          VARCHAR(50)     NOT NULL,
    codigo_ean          VARCHAR(20),
    categoria           VARCHAR(50)     NOT NULL,
    especie_animal      VARCHAR(20)     NOT NULL,
    tipo_armazenamento  VARCHAR(30)     NOT NULL,
    peso_kg             NUMERIC(10, 3)  NOT NULL,
    preco_custo         NUMERIC(10, 2),
    preco_venda         NUMERIC(10, 2)  NOT NULL,
    fabricante          VARCHAR(100)    NOT NULL,
    requer_receita      BOOLEAN         NOT NULL DEFAULT FALSE,
    controlado_anvisa   BOOLEAN         NOT NULL DEFAULT FALSE,
    tempo_validade_dias INTEGER,
    ativo               BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMP       NOT NULL,
    atualizado_em       TIMESTAMP,
    criado_por          VARCHAR(100),
    atualizado_por      VARCHAR(100),

    CONSTRAINT pk_produtos PRIMARY KEY (id),
    CONSTRAINT uq_produtos_sku UNIQUE (codigo_sku),
    CONSTRAINT uq_produtos_ean UNIQUE (codigo_ean)
);

COMMENT ON TABLE produtos IS 'Catálogo de produtos do PetTrack';
COMMENT ON COLUMN produtos.codigo_sku IS 'Código interno único do produto (ex: RAC-PREM-CAO-20KG)';
COMMENT ON COLUMN produtos.codigo_ean IS 'Código de barras físico do produto';
COMMENT ON COLUMN produtos.tipo_armazenamento IS 'TEMPERATURA_AMBIENTE, REFRIGERADO ou CONGELADO';
COMMENT ON COLUMN produtos.controlado_anvisa IS 'Indica se o produto é regulado pela Anvisa';