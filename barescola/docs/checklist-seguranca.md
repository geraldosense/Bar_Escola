# Checklist de segurança (etapa 10)

| # | Verificação | Estado | Onde |
|---|---|:---:|---|
| 1 | Login obrigatório com 3 papéis (ALUNO, FUNCIONARIO, GESTOR) | ✅ | `web.xml` (`login-config` FORM), utilizadores no ApplicationRealm do WildFly |
| 2 | Palavras-passe nunca guardadas em claro na aplicação | ✅ | `add-user` guarda *hash* em `application-users.properties` |
| 3 | Páginas protegidas por papel | ✅ | `web.xml`: `/aluno/*`, `/balcao/*`, `/backoffice/*` |
| 4 | API protegida por papel (defesa em profundidade: REST **e** EJB) | ✅ | `@RolesAllowed` nos recursos JAX-RS e nos EJB |
| 5 | Um aluno só vê e cancela as suas encomendas | ✅ | `EncomendaService.verificarDono()` usa o *principal* do contentor, nunca um id enviado pelo cliente |
| 6 | O aluno não escolhe o preço nem o total | ✅ | O servidor lê o preço da BD; o cliente só envia `produtoId` + `quantidade` |
| 7 | Validação de dados no servidor | ✅ | Bean Validation nas entidades e DTOs (`@NotNull`, `@Min`, `@DecimalMin`…) |
| 8 | Pagamento atómico (sem saldo/stock "meio descontado") | ✅ | Transação JTA + `@ApplicationException(rollback = true)` |
| 9 | Concorrência (duas compras do último produto) | ✅ | `PESSIMISTIC_WRITE` no pagamento + `@Version` |
| 10 | Erros sem *stack traces* para o cliente | ✅ | `EJBExceptionMapper` devolve JSON genérico e regista o detalhe no log |
| 11 | Proteção contra XSS | ✅ | `esc()` em `common.js`, `<c:out>` na JSP, JSF escapa por omissão |
| 12 | SQL injection | ✅ | Apenas JPQL com parâmetros (`setParameter`) |
| 13 | Cookie de sessão `HttpOnly`, só por cookie (sem `;jsessionid` no URL) | ✅ | `web.xml` `session-config` |
| 14 | Cabeçalhos de segurança | ✅ | `SegurancaHeadersFilter` (`nosniff`, `X-Frame-Options`, `Referrer-Policy`, `no-store` na API) |
| 15 | WebSockets autenticados e por papel | ✅ | `/ws/aluno` e `/ws/balcao` protegidos no `web.xml`; o aluno só recebe avisos das suas encomendas |
| 16 | Logout invalida a sessão (POST) | ✅ | `LogoutServlet` |
| 17 | HTTPS e cookie `Secure` | ⚠️ produção | Configurar certificado no WildFly e `<secure>true</secure>` |
| 18 | Serviço SOAP autenticado | ⚠️ produção | Em produção proteger `CartaoWS` com WS-Security ou BASIC + HTTPS (rede interna da escola) |
| 19 | CSRF | ⚠️ parcial | API só aceita JSON (exige *preflight* CORS entre sites); JSF usa `ViewState`. Em produção: `SameSite=Strict` no cookie |

## Testes manuais

- [ ] Abrir `/backoffice/produtos.xhtml` com um aluno → 403.
- [ ] `GET /api/encomendas` com um aluno → 403.
- [ ] `GET /api/encomendas/{id}` de outro aluno → 403 `ACESSO_NEGADO`.
- [ ] `POST /api/encomendas/{id}/cancelar` de outro aluno → 403.
- [ ] Produto com nome `<script>alert(1)</script>` aparece como texto no menu.
- [ ] Sem login, `/api/me` redireciona para o login.
