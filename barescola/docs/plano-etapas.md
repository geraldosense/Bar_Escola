# Plano por etapas, Git e Kanban

O guia pede **tags `etapa-1` a `etapa-10`**, **commits de todos os elementos** e um **quadro Kanban**.
Esta tabela diz que ficheiros pertencem a cada etapa, para que cada elemento da equipa faça os
commits da sua parte (e saiba explicá-la na defesa oral).

| Etapa | Tag | Ficheiros principais | "Pronto quando" (verificação) |
|---|---|---|---|
| 1 · Jakarta EE | `etapa-1` | `pom.xml`, `.vscode/`, `config/JaxRsConfig`, `rest/EstadoResource`, `dto/EstadoAppDTO`, `docs/diagramas/arquitetura.md`, `WEB-INF/jboss-web.xml`, `beans.xml` | `GET /barescola/api/estado` responde no browser |
| 2 · HTML/CSS/JS | `etapa-2` | `css/app.css`, `prototipo/menu-estatico.html`, `js/prototipo.js` | Juntar/remover produtos atualiza o total |
| 3 · Servlets/JSP | `etapa-3` | `web/MenuServlet`, `web/CarrinhoServlet`, `web/EncomendaServlet`, `web/Carrinho`, `WEB-INF/jsp/menu.jsp` | Encomenda criada a partir do carrinho da sessão (`/barescola/menu`) |
| 4 · JPA | `etapa-4` | `model/*`, `META-INF/persistence.xml`, `config/DadosIniciais`, `service/VendasService`, `scripts/configurar-wildfly.cli` | Dados na BD; `GET /api/vendas?data=…` |
| 5 · EJB | `etapa-5` | `service/EncomendaService` (RN1–RN4), `service/exception/*`, `timer/FechoDiaTimer` (RN5) | Sem saldo ou sem stock: nada fica alterado |
| 6 · JAX-RS | `etapa-6` | `rest/*`, `rest/mapper/*`, `dto/*`, `aluno/menu.html`, `js/common.js`, `js/menu.js`, `docs/postman/` | Menu com dados reais; coleção Postman completa |
| 7 · JSF/WebSockets | `etapa-7` | `jsf/*`, `backoffice/*.xhtml`, `WEB-INF/templates/`, `websocket/*`, `aluno/pedidos.html`, `js/pedidos.js`, `balcao/fila.html`, `js/balcao.js` | Funcionário marca pronta e o aluno vê o aviso sem recarregar |
| 8 · JMS | `etapa-8` | `jms/CozinhaProducer`, `jms/CozinhaMDB` | Pagamento responde logo; estado muda segundos depois |
| 9 · SOAP | `etapa-9` | `soap/*`, `docs/soap/` | Teste válido e inválido no SoapUI |
| 10 · Segurança | `etapa-10` | `web.xml` (constraints), `@RolesAllowed`, `web/SegurancaHeadersFilter`, `web/LogoutServlet`, `web/InicioServlet`, `login.html`, `scripts/setup-wildfly.*`, `docs/checklist-seguranca.md` | Checklist de segurança completa |

## Comandos Git

```bash
git init
git branch -M main
git remote add origin https://github.com/<equipa>/barescola.git

# por cada etapa (cada elemento faz commit dos seus ficheiros, com o seu nome/email)
git add <ficheiros da etapa>
git commit -m "etapa 1: projeto Jakarta EE e endpoint /api/estado"
git tag -a etapa-1 -m "Etapa 1 – Jakarta EE"

git push -u origin main --tags
```

Configurar o autor em cada computador (para os commits contarem para cada pessoa):

```bash
git config user.name "Nome Apelido"
git config user.email "email@escola.pt"
```

## Quadro Kanban (GitHub Projects)

Colunas: **Backlog → A fazer → Em curso → Revisão → Feito**. Um cartão por user story e por etapa:

- US01 Ver o menu do dia com preços e disponibilidade
- US02 Carrinho e encomenda para um intervalo
- US03 Pagar com o saldo e ver o saldo restante
- US04 Aviso de pedido pronto
- US05 Fila do balcão por intervalo e ordem de chegada
- US06 Gestão de produtos, preços e stock
- US07 Vendas do dia
- RN1–RN5 (um cartão por regra, ligado ao teste que a demonstra)
- Docs: diagrama ER, diagrama de estados, Postman, SoapUI, WSDL, manual
- Demo Day: guião + ensaio com encomenda feita pelo público
