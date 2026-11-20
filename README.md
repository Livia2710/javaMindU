# 🧠 MindU — API

API do MindU, plataforma universitária de saúde mental corporativa, desenvolvida em Java e Spring Boot para os clientes React e Android.

🌐 **[Abrir a documentação da API](https://docsmindu.vercel.app/)**

📚 **[Abrir documentação, modelagem e coleção Postman](https://livia2710.github.io/docsMindU/)**

## Ambientes

| Ambiente | URL base | Observação |
| :--- | :--- | :--- |
| Local | `http://localhost:8080/` | API iniciada pelo Compose local. |
| Produção | `https://mindu-api.onrender.com` | A primeira requisição após período sem uso pode levar cerca de 1 minuto devido ao cold start do Render. |

## Executar localmente

O procedimento oficial inicia a API e o PostgreSQL sem exigir o código da Mila.

Pré-requisitos: Docker com Docker Compose v2. Na raiz do repositório, crie seu arquivo `.env` a partir do exemplo e preencha uma senha local para o administrador:

```bash
cp .env.example .env
```

No PowerShell, use `Copy-Item .env.example .env`. O `MINDU_ADMIN_SENHA` é obrigatório para o perfil local. Não versione nem compartilhe o `.env`.

Inicie API e banco com:

```bash
docker compose -f docker-compose.local.yml up --build
```

A API ficará em `http://localhost:8080/`. O banco usa o volume `mindu_local_pgdata`. Para encerrar, pressione Ctrl+C e execute `docker compose -f docker-compose.local.yml down`; isso preserva o volume do banco.

### Variáveis locais

| Variável | Obrigatória | Uso |
| :--- | :--- | :--- |
| `MINDU_ADMIN_SENHA` | Sim | Senha inicial do administrador local. |
| `MILA_API_KEY` | Não | Chave usada pela API ao chamar a Mila. Sem Mila, as rotas `/v1/chat/**` retornam 502 no formato padrão. |
| `CORS_ORIGENS` | Não | Origens de navegador separadas por vírgula; por padrão: `http://localhost:5173`, `http://localhost:3000` e `https://mind-u-pi.vercel.app`. |
| `MINDU_LOGIN_LIMITE_POR_MINUTO` | Não | Limite de login por IP; padrão 10 por minuto. |
| `MILA_BUILD_CONTEXT` | Não | Caminho para o clone da Mila, necessário ao ativar o profile `mila`. |
| `MILA_ENV_FILE` | Não | Caminho do arquivo de ambiente da Mila, necessário ao ativar o profile `mila`. |
| `GROQ_API_KEY` | Não | Chave usada pela Mila quando sua configuração exigir Groq. Deve ficar somente no ambiente local. |

### Ativar a Mila (opcional)

A integração Mila é um serviço separado. Para executá-la, disponibilize o repositório Mila localmente e configure `MILA_BUILD_CONTEXT` com a pasta que contém o Dockerfile e `MILA_ENV_FILE` com o caminho do arquivo de ambiente esperado pela Mila. Configure também `MILA_API_KEY` e as chaves requeridas por essa integração no arquivo local, nunca em arquivos versionados. Por exemplo, no `.env`:

```dotenv
MILA_BUILD_CONTEXT=../Mila
MILA_ENV_FILE=../Mila/.env
```

Ative o profile ao iniciar o Compose:

```bash
docker compose -f docker-compose.local.yml --profile mila up --build
```

O contexto padrão é `./mila` e o arquivo de ambiente padrão é `./mila.env`; ambos são opcionais enquanto o profile não estiver ativo. O código da Mila não é incluído neste repositório. Sem ele, a API e o PostgreSQL continuam disponíveis; somente os endpoints `/v1/chat/**` dependentes da integração falham com 502.

O `docker-compose.yml` da raiz é configuração auxiliar de infraestrutura (PostgreSQL, pgAdmin e Mila opcional), não inicia a API. Para o fluxo cotidiano, use exclusivamente o comando `docker-compose.local.yml` acima. A Mila auxiliar no Compose da raiz também utiliza o profile `mila` e as mesmas variáveis de contexto e arquivo de ambiente.

## Formato de erros

As respostas de erro seguem JSON com `erro`, uma mensagem legível. Erros de validação incluem também `campos`, um mapa de nome do campo para mensagem. Por exemplo: `{"erro":"Erro de validação","campos":{"email":"deve ser um endereço de e-mail válido"},"status":400}`.

| Status | Significado |
| :--- | :--- |
| 400 | Requisição inválida, JSON malformado, parâmetro incorreto ou validação; pode incluir `campos`. |
| 401 | Não autenticado ou token inválido/expirado. |
| 403 | Usuário autenticado sem permissão para o recurso. |
| 404 | Recurso ou rota não encontrado. |
| 409 | Conflito com os dados existentes ou regra de integridade. |
| 413 | Arquivo ou requisição maior que o limite aceito. |
| 429 | Limite de tentativas de login excedido; inclui `Retry-After`. |
| 502 | Falha na integração com a Mila ou indisponibilidade do serviço externo. |

## CORS

`CORS_ORIGENS` configura as origens de navegador aceitas, separadas por vírgula. O padrão contempla `http://localhost:5173`, `http://localhost:3000` e `https://mind-u-pi.vercel.app`. Para habilitar um preview da Vercel ou outro domínio, inclua sua URL completa nessa variável, por exemplo `CORS_ORIGENS=http://localhost:5173,https://seu-preview.vercel.app`, e reinicie a API.

O navegador pode ler os headers expostos `X-Total-Count`, `X-Page`, `X-Page-Size`, `X-Total-Pages` e `Retry-After`. Os quatro primeiros contêm metadados de paginação; `Retry-After` informa quando tentar novamente após um 429. O aplicativo Android nativo usa Retrofit/OkHttp e não depende de CORS; CORS é uma regra aplicada pelos navegadores.

## Aplicativo Android

No emulador Android, use a base URL `http://10.0.2.2:8080/`, que encaminha para o computador hospedeiro. `localhost` dentro do emulador aponta para o próprio emulador, não para a API no computador.

Em configurações comuns, Android 9 ou posterior bloqueia tráfego HTTP sem TLS por padrão. No repositório Android, configure um `network security config` que libere HTTP somente para `10.0.2.2` e somente no build de debug. Essa configuração pertence ao aplicativo Android e não é feita neste repositório da API. Em produção, use HTTPS.

## Autenticação e rate limit

O JWT expira em uma hora e não há refresh token. O front React deve tratar 401 levando a pessoa usuária de volta à tela de login. No Android, armazene o token em `EncryptedSharedPreferences`.

O login permite por padrão 10 tentativas por minuto por IP. Ao exceder o limite, a API responde 429 e inclui o header `Retry-After`. Para alterar o limite, configure `mindu.login.limite-por-minuto`; no Compose local, use a variável `MINDU_LOGIN_LIMITE_POR_MINUTO` no `.env` (por exemplo, `MINDU_LOGIN_LIMITE_POR_MINUTO=20`) e recrie a API.

## Planos, dashboard e solicitação de exclusão

`GET /v1/planos` retorna os limites de matrículas, o preço mensal por colaborador e `consultasSemLimite: true` em todos os ambientes. Os preços-base configurados são R$ 129 (Start), R$ 109 (Essencial) e R$ 89 (Corporativo). São valores fictícios para o projeto. A API não aplica cobrança nem bloqueia agendamentos por quantidade de consultas.

As consultas permanecem sem limite neste projeto fictício. Para atendimentos virtuais ou híbridos, o profissional associa um link HTTPS externo ao agendamento com `PATCH /v1/agendamentos/{id}/link-online`; o link aparece nos dados do agendamento dos participantes. Remarcações e cancelamentos são aceitos sem multa até o início da consulta. A duração configurada é de 50 minutos.

O cadastro profissional exige uma carteirinha PNG ou JPEG/JPG de até 5 MB. Após rejeição, o próprio profissional pode reenviá-la por `PATCH /v1/profissionais/{id}/carteirinha`; o status volta para pendente, e a API administrativa retorna o prazo de revisão de até 72 horas corridas desde o envio.

O dashboard da empresa inclui o preço por colaborador, a estimativa mensal considerando o limite de vagas do plano, início/fim de vigência e dia de pagamento. Essa estimativa pressupõe contratação de todas as vagas do plano. Um administrador configura esses dados com `PATCH /v1/admin/empresas/{id}/contrato`, enviando `inicioVigencia`, `fimVigencia` (opcional) e `diaPagamento` (de 1 a 28).

Uma conta autenticada pode abrir uma solicitação com `POST /v1/conta/solicitacao-exclusao`. A conta é desativada imediatamente e a solicitação fica registrada para análise em `GET /v1/admin/solicitacoes-exclusao`; o administrador atualiza seu status em `PATCH /v1/admin/solicitacoes-exclusao/{id}`. A API não apaga automaticamente dados relacionados; a exclusão definitiva após análise ainda é um procedimento administrativo. A recuperação “esqueci minha senha” ainda não está disponível.

## Testes e modelagem

Na [página de documentação](https://livia2710.github.io/docsMindU/) você também encontrará os botões para baixar a coleção do Postman e o artefato de modelagem do banco, regras de negócio e diagramas de casos de uso.
