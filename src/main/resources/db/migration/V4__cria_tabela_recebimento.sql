CREATE TABLE ordens_recebimento (
            id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
            numero_ordem            VARCHAR(30)     NOT NULL,
            nota_fiscal             VARCHAR(50)     NOT NULL,
            fornecedor              VARCHAR(150)    NOT NULL,
            responsavel_id          UUID,
            status                  VARCHAR(30)     NOT NULL DEFAULT 'AGUARDANDO_DESCARGA',
            data_chegada            TIMESTAMP       NOT NULL,
            data_finalizacao        TIMESTAMP,
            observacoes             VARCHAR(500),
            criado_em               TIMESTAMP       NOT NULL,
            atualizado_em           TIMESTAMP,
            criado_por              VARCHAR(100),
            atualizado_por          VARCHAR(100),

            CONSTRAINT pk_ordens_recebimento PRIMARY KEY (id),
            CONSTRAINT uq_ordens_recebimento_numero UNIQUE (numero_ordem),
            CONSTRAINT fk_ordens_recebimento_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuarios(id)
);

CREATE TABLE itens_recebimento (
           id                      UUID             NOT NULL DEFAULT gen_random_uuid(),
           ordem_recebimento_id    UUID             NOT NULL,
           produto_id              UUID             NOT NULL,
           baia_destino_id         UUID,
           quantidade_esperada     INTEGER          NOT NULL,
           quantidade_recebida     INTEGER,
           numero_lote             VARCHAR(50),
           data_fabricacao         DATE,
           data_validade           DATE,
           status                  VARCHAR(20)      NOT NULL DEFAULT 'PENDENTE',
           observacoes             VARCHAR(300),
           criado_em               TIMESTAMP        NOT NULL,
           atualizado_em           TIMESTAMP,
           criado_por              VARCHAR(100),
           atualizado_por          VARCHAR(100),

           CONSTRAINT pk_itens_recebimento PRIMARY KEY (id),
           CONSTRAINT fk_itens_recebimento_ordem FOREIGN KEY (ordem_recebimento_id) REFERENCES ordens_recebimento(id),
           CONSTRAINT fk_itens_recebimento_produto FOREIGN KEY (produto_id) REFERENCES produtos(id),
           CONSTRAINT fk_itens_recebimento_baia FOREIGN KEY (baia_destino_id) REFERENCES baias(id)
);

CREATE INDEX idx_itens_recebimento_ordem ON itens_recebimento(ordem_recebimento_id);
CREATE INDEX idx_itens_recebimento_produto ON itens_recebimento(produto_id);
CREATE INDEX idx_ordens_recebimento_status ON ordens_recebimento(status);

COMMENT ON TABLE ordens_recebimento IS 'Ordens de recebimento de produtos no CD';
COMMENT ON TABLE itens_recebimento IS 'Itens individuais de cada ordem de recebimento';
COMMENT ON COLUMN itens_recebimento.quantidade_esperada IS 'Quantidade prevista na nota fiscal';
COMMENT ON COLUMN itens_recebimento.quantidade_recebida IS 'Quantidade efetivamente conferida pelo funcionário';
COMMENT ON COLUMN itens_recebimento.baia_destino_id IS 'Preenchida pelo funcionário ao aprovar o item';