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

---

## 🧪 Testes e Modelagem

Na [página de documentação](https://livia2710.github.io/docsMindU/) deste projeto, você também encontrará os botões para baixar os arquivos de suporte técnico:
* **Coleção do Postman:** Roteiro completo com **43 requisições pré-configuradas** (incluindo testes de sucesso e testes negativos para validação das regras de negócio).
* **Artefato de Modelagem:** Detalhes das entidades do banco de dados, regras de negócio e diagramas de casos de uso.
