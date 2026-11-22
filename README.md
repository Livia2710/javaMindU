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
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM` | Para enviar e-mail | Credenciais SMTP já configuradas para a Brevo. Mantenha os valores reais apenas no `.env` local ou no ambiente do serviço. |
| `MINDU_RESET_PASSWORD_URL` | Para montar o link | URL da página do front que receberá o token como `?token=...`; localmente, use a rota de redefinição disponibilizada pelo front. |

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

### Recuperação de senha

O processo usa dois endpoints públicos. Primeiro, envie `POST /v1/auth/esqueci-senha` com `{"email":"usuario@email.com"}`. A API sempre responde `200` com a mesma mensagem, mesmo se o e-mail não existir. Para uma conta ativa, cria um token aleatório de uso único, guarda somente o hash no banco e envia o link para o endereço cadastrado. O link expira em 15 minutos.

```json
{"mensagem":"Se o e-mail estiver cadastrado, enviaremos um link para redefinir sua senha."}
```

O link usa `MINDU_RESET_PASSWORD_URL` como endereço da página do front e acrescenta `?token=...`. O envio acontece de forma assíncrona, após o token ser gravado, para que a chamada HTTP não dependa da latência SMTP. Mesmo se a Brevo falhar, a resposta continua genérica; a falha fica no log do servidor e o token não entregue é invalidado. A tela React ou Android que abrir esse link deve ler o token e enviar a senha nova para `POST /v1/auth/redefinir-senha`:

```json
{"token":"token-recebido-no-link","novaSenha":"NovaSenha123!"}
```

O sucesso retorna `204 No Content`; token inválido, expirado ou já utilizado retorna `400` JSON. A senha nova segue a regra existente de no mínimo 8 caracteres. O consumo do token e a alteração da senha são transacionais; a API invalida os outros links pendentes e incrementa a versão do usuário, encerrando sessões JWT anteriores. A limitação atual de 10 requisições por minuto por IP também protege os dois endpoints de recuperação.

Para testar localmente, mantenha no `.env` as credenciais SMTP da Brevo já configuradas (em um clone novo, copie os nomes do `.env.example` e preencha os valores SMTP), confirme que `SMTP_FROM` é um remetente autorizado e que `MINDU_RESET_PASSWORD_URL` aponta para a página de redefinição usada pelo front. Suba a API com o Compose local e solicite o e-mail para uma caixa de teste que você controla:

```bash
curl -i -X POST http://localhost:8080/v1/auth/esqueci-senha \
  -H "Content-Type: application/json" \
  -d '{"email":"SEU_EMAIL_DE_TESTE"}'
```

Abra o link recebido, copie o token e envie a nova senha (o front fará a mesma chamada):

```bash
curl -i -X POST http://localhost:8080/v1/auth/redefinir-senha \
  -H "Content-Type: application/json" \
  -d '{"token":"TOKEN_RECEBIDO_NO_EMAIL","novaSenha":"NovaSenha123!"}'
```

O resultado esperado é `204 No Content`. Depois, confirme que a nova senha permite login e que um token JWT anterior já não autentica. Os testes automatizados usam mock e não enviam e-mails reais. Não compartilhe o link/token: qualquer pessoa que o possua pode redefinir a senha até seu uso ou expiração.

**Limitação de hospedagem:** o plano gratuito do Render bloqueia tráfego SMTP de saída nas portas 25, 465 e 587. Portanto, o SMTP da Brevo pode ser testado localmente, mas o envio não funcionará no serviço gratuito atual sem uma hospedagem que permita SMTP. [Limitações de rede do Render Free](https://render.com/docs/free).

## Planos, dashboard e solicitação de exclusão

`GET /v1/planos` retorna os limites de matrículas, o preço mensal por colaborador e `consultasSemLimite: true` em todos os ambientes. Os preços-base configurados são R$ 129 (Start), R$ 109 (Essencial) e R$ 89 (Corporativo). São valores fictícios para o projeto. A API não aplica cobrança nem bloqueia agendamentos por quantidade de consultas.

As consultas permanecem sem limite neste projeto fictício. Para atendimentos virtuais ou híbridos, o profissional associa um link HTTPS externo ao agendamento com `PATCH /v1/agendamentos/{id}/link-online`; o link aparece nos dados do agendamento dos participantes. Remarcações e cancelamentos são aceitos sem multa até o início da consulta. A duração configurada é de 50 minutos.

O cadastro profissional exige uma carteirinha PNG ou JPEG/JPG de até 5 MB. Após rejeição, o próprio profissional pode reenviá-la por `PATCH /v1/profissionais/{id}/carteirinha`; o status volta para pendente, e a API administrativa retorna o prazo de revisão de até 72 horas corridas desde o envio.

O dashboard da empresa inclui o preço por colaborador, a estimativa mensal considerando o limite de vagas do plano, início/fim de vigência e dia de pagamento. Essa estimativa pressupõe contratação de todas as vagas do plano. Um administrador configura esses dados com `PATCH /v1/admin/empresas/{id}/contrato`, enviando `inicioVigencia`, `fimVigencia` (opcional) e `diaPagamento` (de 1 a 28).

Uma conta autenticada pode abrir uma solicitação com `POST /v1/conta/solicitacao-exclusao`. A conta é desativada imediatamente e a solicitação fica registrada para análise em `GET /v1/admin/solicitacoes-exclusao`; o administrador atualiza seu status em `PATCH /v1/admin/solicitacoes-exclusao/{id}`. A API não apaga automaticamente dados relacionados; a exclusão definitiva após análise ainda é um procedimento administrativo.

## Testes e modelagem

Na [página de documentação](https://livia2710.github.io/docsMindU/) você também encontrará os botões para baixar a coleção do Postman e o artefato de modelagem do banco, regras de negócio e diagramas de casos de uso.
