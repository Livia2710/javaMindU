-- Executado automaticamente no perfil prod antes da validação do Hibernate.
-- As operações são idempotentes para que possam rodar a cada inicialização.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS cep VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS logradouro VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS numero VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS complemento VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS bairro VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS cidade VARCHAR(255);
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS estado VARCHAR(255);
ALTER TABLE profissional ADD COLUMN IF NOT EXISTS bio VARCHAR(2000);
ALTER TABLE profissional ALTER COLUMN tipo DROP NOT NULL;
ALTER TABLE profissional ALTER COLUMN foto_carterinha DROP NOT NULL;
ALTER TABLE agendamento ADD COLUMN IF NOT EXISTS modalidade VARCHAR(255);
