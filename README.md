# 🧠 MindU — Documentação da API

Este é o repositório da API do **MindU** (plataforma de saúde mental corporativa), desenvolvida em **Java** com **Spring Boot**. Ela fornece os serviços e endpoints necessários para abastecer as aplicações front-end (React) e mobile (Android).

🌐 **[Abrir a Documentação Completa](https://docsmindu.vercel.app/)**

---

## 🚀 Ambientes da API

| Ambiente | URL Base | Observação |
| :--- | :--- | :--- |
| **💻 Local** | `http://localhost:8080` | Ambiente de desenvolvimento na sua máquina. |
| **🌐 Web (Produção)** | `https://onrender.com` | Servidor online. *Nota: a primeira requisição do dia pode demorar cerca de 1 minuto para responder devido ao cold start do Render.* |

---

## 🛠️ Como Executar o Projeto Localmente

A forma mais rápida de rodar a API e o banco de dados é utilizando o **Docker**.

### Pré-requisitos
* Docker e Docker Compose instalados.

### Passo a Passo
1. Clone este repositório em sua máquina.
2. Na raiz do projeto, execute o comando abaixo para subir a API e o banco de dados PostgreSQL:

```bash
docker compose up
```

Após o carregamento, a API estará pronta e respondendo em `http://localhost:8080`.

### Planos, dashboard e solicitação de exclusão

`GET /v1/planos` retorna os limites de matrículas, o preço mensal por colaborador e `consultasSemLimite: true` em todos os ambientes. Os preços-base configurados são R$ 129 (Start), R$ 109 (Essencial) e R$ 89 (Corporativo). São valores fictícios para o projeto. A API não aplica cobrança nem bloqueia agendamentos por quantidade de consultas.

As consultas permanecem sem limite em todos os ambientes deste projeto fictício. Para atendimentos virtuais ou híbridos, o profissional associa um link HTTPS externo ao agendamento com `PATCH /v1/agendamentos/{id}/link-online`; o link aparece nos dados do agendamento dos participantes. Remarcações e cancelamentos são aceitos sem multa até o início da consulta. A duração configurada é de 50 minutos.

O cadastro profissional exige uma carteirinha PNG ou JPEG/JPG de até 5 MB. Após rejeição, o próprio profissional pode reenviá-la por `PATCH /v1/profissionais/{id}/carteirinha`; o status volta para pendente, e a API administrativa retorna o prazo de revisão de até 72 horas corridas desde o envio.

O dashboard da empresa inclui o preço por colaborador, a estimativa mensal considerando o limite de vagas do plano, início/fim de vigência e dia de pagamento. Essa estimativa pressupõe contratação de todas as vagas do plano. Um administrador configura esses dados com `PATCH /v1/admin/empresas/{id}/contrato`, enviando `inicioVigencia`, `fimVigencia` (opcional) e `diaPagamento` (de 1 a 28).

Uma conta autenticada pode abrir uma solicitação com `POST /v1/conta/solicitacao-exclusao`. A conta é desativada imediatamente e a solicitação fica registrada para análise em `GET /v1/admin/solicitacoes-exclusao`; o administrador atualiza seu status em `PATCH /v1/admin/solicitacoes-exclusao/{id}`. A API não apaga automaticamente dados relacionados; a exclusão definitiva após análise ainda é um procedimento administrativo. A recuperação “esqueci minha senha” ainda não está disponível.

---

## 🧪 Testes e Modelagem

Na [página de documentação](https://livia2710.github.io/docsMindU/) deste projeto, você também encontrará os botões para baixar os arquivos de suporte técnico:
* **Coleção do Postman:** Roteiro completo com **45 requisições pré-configuradas** (incluindo testes de sucesso e testes negativos para validação das regras de negócio).
* **Artefato de Modelagem:** Detalhes das entidades do banco de dados, regras de negócio e diagramas de casos de uso.
