CREATE TABLE usuarios (
        id              UUID            NOT NULL DEFAULT gen_random_uuid(),
        nome            VARCHAR(120)    NOT NULL,
        email           VARCHAR(100)    NOT NULL,
        senha           VARCHAR(255)    NOT NULL,
        matricula       VARCHAR(30)     NOT NULL,
        perfil          VARCHAR(50)     NOT NULL,
        ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
        criado_em       TIMESTAMP       NOT NULL,
        atualizado_em   TIMESTAMP,
        criado_por      VARCHAR(100),
        atualizado_por  VARCHAR(100),

        CONSTRAINT pk_usuarios PRIMARY KEY (id),
        CONSTRAINT uq_usuarios_email UNIQUE (email),
        CONSTRAINT uq_usuarios_matricula UNIQUE (matricula)
);

COMMENT ON TABLE usuarios IS 'Usuários e funcionários do PetTrack';
COMMENT ON COLUMN usuarios.perfil IS 'ADMIN, OPERADOR_RECEBIMENTO, OPERADOR_ESTOQUE, OPERADOR_ECOMMERCE, OPERADOR_TRANSPORTE, OPERADOR_FILIAL, MOTORISTA, GESTOR';
COMMENT ON COLUMN usuarios.matricula IS 'Código interno do funcionário';
COMMENT ON COLUMN usuarios.senha IS 'Senha criptografada com BCrypt';

INSERT INTO usuarios (id, nome, email, senha, matricula, perfil, ativo, criado_em)
VALUES (
        gen_random_uuid(),
        'Administrador',
        'admin@pettrack.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'ADM001',
        'ADMIN',
        true,
        NOW()
       );