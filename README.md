# Restaurante API

Nome: Kaio Vinicius Corredor da Silva
Vídeo no youtube: https://youtu.be/jR0y4JU0fU0

API REST de pedidos de restaurante feita em **Kotlin + Spring Boot 4 + JPA + JWT**.
Trabalho da disciplina (Opção 1 — estendendo os serviços vistos em aula).

## Sumário

1. [O que esse projeto entrega (em relação ao enunciado)](#o-que-esse-projeto-entrega)
2. [Arquitetura](#arquitetura)
3. [Como rodar](#como-rodar)
4. [Como testar (passo a passo)](#como-testar-passo-a-passo)
5. [Endpoints](#endpoints)
6. [Modelo de dados](#modelo-de-dados)
7. [Segurança](#seguranca)
8. [Logs e tratamento de erros](#logs-e-tratamento-de-erros)

---

## O que esse projeto entrega

Pedidos do enunciado (Opção 1):

| Requisito | Onde está |
| --- | --- |
| 2+ classes novas com relação one-to-many ou many-to-many | `Produto` ⇄ `Pedido` (Many-to-Many via tabela `PedidoProduto`) |
| CRUD completo de pelo menos uma classe | CRUD completo de `Produto` (`POST`, `GET`, `GET/{id}`, `PUT`, `DELETE`) |
| Autenticação para ações destrutivas | JWT obrigatório em `POST/PUT/DELETE`. `DELETE` exige role `ADMIN` |
| Endpoints para associar as duas classes | `PUT /pedidos/{pedidoId}/produtos/{produtoId}` e `DELETE` correspondente |
| Endpoint de consulta com filtros/ordenação por query params | `GET /produtos?categoria=...&disponivel=...&precoMin=...&precoMax=...&sortBy=...&sortDir=...` |
| Logs nos serviços | SLF4J em `ProdutoService`, `PedidoService` e `UserService` |
| Exceções nos serviços | `BadRequestException`, `NotFoundException`, `UnauthorizedException`, `ForbiddenException` |
| Validações | Bean Validation (`@NotBlank`, `@DecimalMin`, `@Email`, `@Pattern`) nas requests |

---

## Arquitetura

```
src/main/kotlin/br/pucpr/restaurante/
├── RestauranteApplication.kt        # entry point Spring Boot
├── Bootstrapper.kt                  # cria admin + roles + catálogo inicial
├── exceptions/                      # 4 exceções com @ResponseStatus
├── security/
│   ├── Jwt.kt                       # cria e valida tokens (jjwt 0.13)
│   ├── UserToken.kt                 # claim com id, name, roles
│   ├── JwtFilter.kt                 # OncePerRequestFilter — autentica via Bearer
│   └── SecurityConfig.kt            # libera rotas públicas, exige JWT no resto
├── users/                           # autenticação + cadastro de usuários
├── roles/                           # ADMIN, CLIENT
├── produtos/                        # ★ nova classe: catálogo do restaurante
└── pedidos/                         # ★ nova classe: pedidos com produtos (M-N)
```

Cada módulo de domínio segue o mesmo padrão da aula: `Entity + Repository + Service + Controller + requests/ + responses/`.

---

## Como rodar

### Pré-requisitos
- macOS / Linux / Windows
- **Java 17+** (testado com Temurin 17)
- **Gradle 9+** (no Mac: `brew install gradle`)

### Subir o servidor

```bash
cd restaurante-api
gradle bootRun
```

Pronto. O servidor sobe em `http://localhost:8080/api`.

> Se você tiver mais de uma JDK, force a versão 17 com:
> `JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle bootRun`

### Recursos disponíveis

- **API**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **H2 Console** (banco em memória): `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:db` — user: `sa` — pass: `sa`
- **Logs em arquivo**: `./logs/`

### Usuário admin pré-criado

Toda vez que o servidor sobe (com banco vazio), é criado:

```
email:    admin@restaurante.com
senha:    Admin@123
roles:    ADMIN
```

---

## Como testar (passo a passo)

Os exemplos abaixo usam `curl`. Você também pode testar tudo via Swagger.

### 1. Login e captura do token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@restaurante.com","password":"Admin@123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')
```

### 2. Listar produtos (público — não precisa de token)

```bash
curl http://localhost:8080/api/produtos
```

### 3. Filtragem e ordenação

```bash
# pizzas, ordenadas por preço decrescente
curl "http://localhost:8080/api/produtos?categoria=PIZZA&sortBy=preco&sortDir=DESC"

# faixa de preço
curl "http://localhost:8080/api/produtos?precoMin=10&precoMax=20"

# só os disponíveis
curl "http://localhost:8080/api/produtos?disponivel=true"
```

### 4. Criar um produto (precisa de token)

```bash
curl -X POST http://localhost:8080/api/produtos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nome":"Lasanha","descricao":"À bolonhesa","preco":38.0,"categoria":"MASSA"}'
```

### 5. Criar um pedido com produtos

```bash
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"cliente":"João","produtoIds":[1,4]}'
```

### 6. Adicionar / remover produto de um pedido

```bash
# adicionar produto 5 ao pedido 1
curl -X PUT http://localhost:8080/api/pedidos/1/produtos/5 \
  -H "Authorization: Bearer $TOKEN"

# remover produto 1 do pedido 1
curl -X DELETE http://localhost:8080/api/pedidos/1/produtos/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Mudar status do pedido

Status válido: `CRIADO → PAGO → ENTREGUE` (ou `CANCELADO` em qualquer ponto antes de ENTREGUE).

```bash
curl -X PATCH http://localhost:8080/api/pedidos/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"PAGO"}'
```

### 8. Filtrar pedidos

```bash
curl "http://localhost:8080/api/pedidos?status=CRIADO&sortBy=criadoEm&sortDir=DESC" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Endpoints

### Autenticação / Usuários

| Método | Path | Auth | Descrição |
| --- | --- | --- | --- |
| POST | `/users` | público | Cadastrar novo usuário (validação de email + senha forte) |
| POST | `/users/login` | público | Retorna `{token, user}` |
| GET | `/users/ping` | público | Health-check |
| GET | `/users/{id}` | autenticado | Buscar usuário |
| GET | `/users` | ADMIN | Listar (`?role=...&sortDir=ASC|DESC`) |
| PATCH | `/users/{id}` | autenticado | Atualizar nome |
| DELETE | `/users/{id}` | ADMIN | Remover (não permite remover o último admin) |
| PUT | `/users/{id}/roles/{roleName}` | ADMIN | Conceder role |

### Produtos (CRUD completo + filtragem)

| Método | Path | Auth | Descrição |
| --- | --- | --- | --- |
| GET | `/produtos` | público | Listar com query params (ver abaixo) |
| GET | `/produtos/{id}` | público | Detalhe |
| POST | `/produtos` | autenticado | Criar produto |
| PUT | `/produtos/{id}` | autenticado | Atualizar |
| DELETE | `/produtos/{id}` | ADMIN | Remover |

**Query params de `GET /produtos`:**
- `categoria` (string) — filtra por categoria, case-insensitive
- `disponivel` (boolean) — só os disponíveis (ou só os indisponíveis)
- `precoMin`, `precoMax` (decimal) — faixa de preço
- `sortBy` (`nome` | `preco` | `categoria`) — campo de ordenação (default `nome`)
- `sortDir` (`ASC` | `DESC`) — direção (default `ASC`)

### Pedidos (CRUD + associação com Produto)

| Método | Path | Auth | Descrição |
| --- | --- | --- | --- |
| POST | `/pedidos` | autenticado | Criar (já com lista de `produtoIds`) |
| GET | `/pedidos` | autenticado | Listar com filtros |
| GET | `/pedidos/{id}` | autenticado | Detalhe |
| PUT | `/pedidos/{pedidoId}/produtos/{produtoId}` | autenticado | **Associar** produto |
| DELETE | `/pedidos/{pedidoId}/produtos/{produtoId}` | autenticado | **Desassociar** produto |
| PATCH | `/pedidos/{id}/status` | autenticado | Mudar status (validação de transição) |
| DELETE | `/pedidos/{id}` | ADMIN | Cancelar/remover (proibido se já entregue) |

---

## Modelo de dados

```
Pedido (1) ──────< PedidoProduto >────── (1) Produto         (Many-to-Many)
   │
   ├ id
   ├ cliente
   ├ status        (CRIADO, PAGO, ENTREGUE, CANCELADO)
   ├ criadoEm
   └ total()       (computado a partir dos produtos)


User (N) ─────< UserRole >───── (M) Role                     (Many-to-Many)
   │
   ├ id
   ├ email (unique)
   ├ password
   └ name
```

---

## Segurança

- **Stateless** (sem sessão). Cada requisição leva o JWT no header `Authorization: Bearer <token>`.
- O token expira em 48h (1h se for admin) e é assinado com HMAC-SHA256.
- O `JwtFilter` lê o header, valida a assinatura, recria o `UserToken` (id + name + roles) e o injeta no `SecurityContext` como `Authentication`.
- O `SecurityConfig` declara as rotas públicas; o resto cai em `anyRequest().authenticated()`.
- `@PreAuthorize("hasRole('ADMIN')")` nos endpoints de `DELETE` e gestão de usuários.
- O dispatcher `ERROR` é liberado para que o forward para `/error` consiga renderizar o JSON da exceção.

### Como o JWT é validado em cada request

```
Request ─► JwtFilter
              │
              ├─ tem header "Bearer ..."?  não → segue sem auth (404/403 vai cair depois)
              ├─ Jwts.parser().verify(token)
              ├─ extrai claim "user" → reconstrói UserToken
              └─ SecurityContextHolder.setAuthentication(...)
            ▼
        @PreAuthorize / authorizeHttpRequests decidem
```

---

## Logs e tratamento de erros

### Logs

Cada service tem `LoggerFactory.getLogger(...)`. Exemplos:

- `UserService.login`: `INFO User {} logged in.`
- `ProdutoService.insert`: `INFO Produto {} ({}) cadastrado.`
- `ProdutoService.delete`: `WARN Produto {} ({}) removido.`
- `PedidoService.updateStatus`: `INFO Pedido {} mudou de {} para {}`

Saída: console (nível INFO+) e arquivo `./logs/...` (nível TRACE+).

### Exceções

Quatro classes (em `exceptions/`) com `@ResponseStatus(...)`:

| Exceção | HTTP | Quando |
| --- | --- | --- |
| `BadRequestException` | 400 | Regra de negócio violada (nome duplicado, status incompatível, etc.) |
| `NotFoundException` | 404 | Entidade não encontrada |
| `UnauthorizedException` | 401 | Login inválido |
| `ForbiddenException` | 403 | Reservada (Spring Security responde 403 antes) |

### Validações nas requests (Bean Validation)

- `CreateUserRequest`: `@NotBlank` em nome, `@Email` no email, `@Pattern` na senha (≥ 8 caracteres, letra + dígito + símbolo)
- `CreateProdutoRequest` / `UpdateProdutoRequest`: `@NotBlank` em nome/categoria, `@DecimalMin(0.0)` no preço
- `CreatePedidoRequest`: `@NotBlank` em cliente
- `UpdateStatusRequest`: `@NotBlank` em status

### Exemplo de erro 400

```json
{
  "timestamp": "2026-05-09T02:39:22.262Z",
  "status": 400,
  "error": "Bad Request",
  "exception": "org.springframework.web.bind.MethodArgumentNotValidException",
  "errors": [{
    "field": "preco",
    "rejectedValue": -5,
    "defaultMessage": "preço deve ser maior que zero"
  }],
  "path": "/api/produtos"
}
```

---

## Resumo das decisões de projeto

| Decisão | Por quê |
| --- | --- |
| Many-to-Many simples (sem entidade de junção `PedidoItem` com quantidade) | Atende ao requisito do enunciado e mantém o código compacto |
| Status do pedido como enum + máquina de estados | Evita pedidos inconsistentes (`ENTREGUE → CRIADO`) |
| Senha em texto plano (sem hash) | Para manter paridade com o material da aula. Em produção: `BCryptPasswordEncoder` |
| Filtros via JPQL com parâmetros opcionais (`:campo is null or ...`) | Solução simples sem precisar de Specifications/QueryDSL |
| `@PreAuthorize` ao invés de só `requestMatchers` | Permite combinar regra por método (ex.: PUT autenticado, DELETE só admin no mesmo controller) |
