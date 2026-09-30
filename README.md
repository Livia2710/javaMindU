# 🧠 MindU — Documentação da API

Central de integração do backend do **MindU** (plataforma de saúde mental corporativa) para as equipes de front-end (React) e mobile (Android).

🌐 **[Abrir a Documentação Completa (GitHub Pages)](https://github.io)**

A página centraliza todas as rotas com seus respectivos métodos, regras de autenticação, exemplos reais de requisição/resposta e trechos de código prontos (com abas dedicadas para React/Vite e Android/Java). Um seletor global no topo da página permite alternar instantaneamente entre os ambientes local e de produção.

---

## 🚀 Ambientes da API

| Ambiente | URL Base | Observação |
| :--- | :--- | :--- |
| **💻 Local** | `http://localhost:8080` | Requer o backend rodando localmente na sua máquina. |
| **🌐 Web (Produção)** | `https://mindu-api.onrender.com` | Sempre ativo. *Nota: o primeiro acesso do dia pode demorar cerca de 1 minuto para iniciar devido ao cold start do servidor.* |

---

## 🛠️ Como Executar o Backend Localmente

O backend é uma API desenvolvida em **Java (Spring Boot)**. A forma mais rápida de subir o ambiente é utilizando o Docker:

```bash
# Iniciar a API e o banco de dados Postgres simultaneamente
docker compose up
```

Após o carregamento, a API estará acessível em `http://localhost:8080`. Para maiores detalhes de configuração, acesse o [Repositório da API Java](https://github.com).

---

## 📥 Recursos para Download

Diretamente na página da documentação, você encontrará botões dedicados para baixar os seguintes arquivos de suporte:

* **Artefato de Modelagem:** Detalhamento completo das entidades, regras de negócio e casos de uso do sistema.
* **Coleção do Postman:** Roteiro de testes integrado com **43 requisições pré-configuradas**, cobrindo o fluxo principal e todos os testes negativos (validação de regras de negócio).

---

## 📂 Estrutura do Repositório

```text
docs/
├── index.html                     ← Página principal da documentação (publicada via GitHub Pages)
├── mindu-modelagem.html           ← Artefato de modelagem técnica (disponível para download)
└── mindu-postman-collection.json  ← Coleção de testes do Postman (disponível para download)
```
