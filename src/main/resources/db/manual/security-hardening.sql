-- Executado no perfil local após o ddl-auto update e no perfil prod antes da validação do Hibernate.
-- As operações são idempotentes para que possam rodar a cada inicialização.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS cep VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS logradouro VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS numero VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS complemento VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS bairro VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS cidade VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS estado VARCHAR(255);
ALTER TABLE profissional ADD COLUMN IF NOT EXISTS bio VARCHAR(2000);
ALTER TABLE profissional ADD COLUMN IF NOT EXISTS carteirinha_enviada_em TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE profissional ALTER COLUMN tipo DROP NOT NULL;
ALTER TABLE profissional ALTER COLUMN foto_carterinha DROP NOT NULL;
ALTER TABLE agendamento ADD COLUMN IF NOT EXISTS modalidade VARCHAR(255);
ALTER TABLE agendamento ADD COLUMN IF NOT EXISTS link_online VARCHAR(2048);
ALTER TABLE plano ADD COLUMN IF NOT EXISTS preco_mensal_por_colaborador NUMERIC(10,2);
ALTER TABLE plano ADD COLUMN IF NOT EXISTS consultas_sem_limite BOOLEAN NOT NULL DEFAULT TRUE;
UPDATE plano SET preco_mensal_por_colaborador = 129.00
 WHERE nome = 'Plano Start' AND preco_mensal_por_colaborador IS NULL;
UPDATE plano SET preco_mensal_por_colaborador = 109.00
 WHERE nome = 'Plano Essencial' AND preco_mensal_por_colaborador IS NULL;
UPDATE plano SET preco_mensal_por_colaborador = 89.00
 WHERE nome = 'Plano Corporativo' AND preco_mensal_por_colaborador IS NULL;
ALTER TABLE plano ALTER COLUMN preco_mensal_por_colaborador SET NOT NULL;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS inicio_vigencia_plano DATE;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS fim_vigencia_plano DATE;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS dia_pagamento INTEGER;
CREATE TABLE IF NOT EXISTS solicitacao_exclusao_conta (
    id VARCHAR(36) PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    tipo_conta VARCHAR(255) NOT NULL,
    solicitada_em TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(255) NOT NULL
);
