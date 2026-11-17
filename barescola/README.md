# BarEscola

Encomendas antecipadas no bar da escola, pagas com o saldo do cartão de estudante.

> Projeto Integrador – UFCD 10791. O aluno encomenda durante a aula anterior, paga com o saldo
> do cartão e levanta no intervalo; o bar prepara os pedidos por ordem e avisa quando estão prontos.

## Equipa

| Nome | Papéis que desempenhou | Contribuições principais |
|------|------------------------|--------------------------|
|      |                        |                          |
|      |                        |                          |
|      |                        |                          |

## Arquitetura

Apresentação (HTML/CSS/JS, JSP, JSF) → API (JAX-RS, SOAP, WebSockets)
→ Negócio (EJB, JMS, Timer) → Persistência (JPA) → Base de dados (H2)

Diagrama completo e descrição dos pacotes: [docs/diagramas/arquitetura.md](docs/diagramas/arquitetura.md)

```
src/main/java/pt/escola/barescola
├── config      JAX-RS, papéis, relógio, dados iniciais
├── model       6 entidades JPA + EstadoEncomenda
├── dto         records de entrada/saída da API
├── service     EJB com as regras de negócio (RN1–RN5) + exceções
├── jms         fila da cozinha (produtor + MDB)
├── timer       fecho do dia (@Schedule)
├── rest        recursos JAX-RS + exception mappers
├── websocket   avisos em tempo real
├── soap        CartaoWS
├── web         servlets, carrinho de sessão, filtros
└── jsf         backing beans do backoffice
src/main/webapp
├── aluno/      menu.html, pedidos.html          (HTML + fetch)
├── balcao/     fila.html                         (ecrã do balcão)
├── backoffice/ produtos, vendas, saldos (.xhtml) (JSF)
├── prototipo/  menu-estatico.html                (etapa 2, dados falsos)
├── WEB-INF/jsp menu.jsp                          (etapa 3, Servlet + JSP)
├── css/ js/    estilos e scripts partilhados
└── login.html
```

## Estados da encomenda

PAGA → EM_PREPARACAO → PRONTA → ENTREGUE (ou CANCELADA)

Diagrama: [docs/diagramas/estados-encomenda.md](docs/diagramas/estados-encomenda.md)

## Como executar

### Requisitos
- **JDK 21**
- **Maven 3.9+**
- **WildFly 34 ou 35** (Jakarta EE 10) – <https://www.wildfly.org/downloads/>
- VS Code com as extensões recomendadas (o VS Code sugere-as ao abrir a pasta – `.vscode/extensions.json`)

### Passos
1. **JDK 21 e WildFly com `standalone-full.xml`** – descompactar o WildFly e definir a variável `WILDFLY_HOME`:
   ```bash
   export WILDFLY_HOME=~/wildfly-35.0.1.Final              # Linux/macOS
   $env:WILDFLY_HOME = "C:\wildfly-35.0.1.Final"           # Windows (PowerShell)
   ```
2. **Configurar a base de dados e os utilizadores** (com o servidor parado; só é preciso uma vez):
   ```bash
   ./scripts/setup-wildfly.sh                                         # Linux/macOS
   powershell -ExecutionPolicy Bypass -File scripts\setup-wildfly.ps1 # Windows
   ```
   Cria o datasource H2 `java:jboss/datasources/BarEscolaDS` (ficheiro em `standalone/data/barescola/`)
   e os utilizadores de teste. As tabelas e os dados de exemplo são criados no primeiro arranque.
3. **Arrancar o WildFly** com o perfil *full* (necessário para JMS):
   ```bash
   $WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml
   ```
4. **`mvn package` e deploy de `target/barescola.war`**:
   ```bash
   mvn clean package wildfly:deploy
   # ou copiar target/barescola.war para $WILDFLY_HOME/standalone/deployments/
   ```
5. **Abrir <http://localhost:8080/barescola>**
6. **Utilizadores de teste**:

   | Papel | Email | Palavra-passe |
   |---|---|---|
   | ALUNO (saldo 15 €) | `aluno1@barescola.pt` | `Aluno#2026` |
   | ALUNO (saldo 2 €) | `aluno2@barescola.pt` | `Aluno#2026` |
   | ALUNO (saldo 8,50 €) | `aluno3@barescola.pt` | `Aluno#2026` |
   | FUNCIONARIO | `balcao@barescola.pt` | `Balcao#2026` |
   | GESTOR | `gestor@barescola.pt` | `Gestor#2026` |

### No VS Code
- **Ctrl+Shift+B** → `Maven: package`
- **Terminal → Run Task…** → `WildFly: configurar`, `WildFly: iniciar (standalone-full, debug 8787)`, `WildFly: deploy`
- **F5** → `Depurar WildFly (attach 8787)` para pôr *breakpoints* no código a correr no servidor
- [`api.http`](api.http) → testar a API com a extensão *REST Client* (botão *Send Request*)

### Modo demonstração
Os intervalos de exemplo são às 10:05, 11:50, 13:20, 15:05 e 16:50. Depois das 16:35 a RN3 bloqueia
novas encomendas. Para demonstrar a qualquer hora, arrancar o WildFly com
`BARESCOLA_MODO_DEMO=true` (ou `-Dbarescola.modoDemo=true`). Ver [docs/demo-day.md](docs/demo-day.md).

### Testes
```bash
mvn test
```

## Funcionalidades (user stories)

- [x] US01 Ver o menu do dia com preços e disponibilidade – `aluno/menu.html`, `GET /api/produtos`
- [x] US02 Juntar produtos a um carrinho e encomendar para um intervalo – `menu.js`, `POST /api/encomendas` (e versão JSP em `/menu`)
- [x] US03 Pagar com o saldo do cartão e ver o saldo restante – `EncomendaService.pagar()`, `PagamentoDTO.saldoRestante`
- [x] US04 Ser avisado quando o pedido estiver pronto – WebSocket `/ws/aluno`, `aluno/pedidos.html`
- [x] US05 Ver as encomendas por intervalo e por ordem de chegada – `balcao/fila.html`, `GET /api/encomendas`
- [x] US06 Gerir produtos, preços e stock – `backoffice/produtos.xhtml`, CRUD `/api/produtos`
- [x] US07 Ver as vendas do dia – `backoffice/vendas.xhtml`, `GET /api/vendas?data=`

## Regras de negócio implementadas

| Regra | Onde está no código |
|---|---|
| **RN1** Só se encomenda se o saldo cobrir o total | `EncomendaService.pagar()` → `Aluno.temSaldoPara()` → `SaldoInsuficienteException` |
| **RN2** Só se encomenda se houver stock; o stock desce ao pagar | `EncomendaService.pagar()` → `Produto.baixarStock()` → `StockInsuficienteException` |
| **RN3** Encomendas fecham 15 min antes do intervalo | `Intervalo.aceitaEncomendas()` (`MINUTOS_FECHO_ANTECIPADO`) → `EncomendasFechadasException` |
| **RN4** Cancelar só em PAGA; saldo e stock repostos | `EncomendaService.cancelar()`, `EstadoEncomenda.podeCancelar()` |
| **RN5** No fim do dia, PRONTA → ENTREGUE com registo | `FechoDiaTimer` (`@Schedule` 23:30) → `EncomendaService.fecharDia()`, campo `entregaAutomatica` + log |

**Transação (RN1 + RN2):** `pagar()` desconta primeiro o saldo e só depois baixa o stock produto a produto.
Se o stock falhar a meio, a `NegocioException` (`@ApplicationException(rollback = true)`) faz o contentor
desfazer tudo – o saldo **não** fica descontado. Os testes de domínio estão em `src/test/java`.

## API

| Método | Endpoint | Papel | Respostas |
|---|---|---|---|
| GET | `/api/estado` | público | 200 |
| GET | `/api/me` | todos | 200 |
| GET | `/api/produtos?ativos=true` | todos | 200 |
| GET | `/api/produtos/{id}` · `/api/produtos/categorias` | todos | 200, 404 |
| POST | `/api/produtos` | GESTOR | 201 + `Location`, 400, 403 |
| PUT | `/api/produtos/{id}` | GESTOR | 200, 400, 404 |
| PATCH | `/api/produtos/{id}/stock` | GESTOR | 200, 400, 404 |
| DELETE | `/api/produtos/{id}` | GESTOR | 204 (apagado), 200 (desativado), 404 |
| GET | `/api/intervalos` | todos | 200 |
| POST | `/api/encomendas` | ALUNO | 201 + `Location`, 400, 404, 409 |
| GET | `/api/encomendas/minhas` | ALUNO | 200 |
| POST | `/api/encomendas/{id}/cancelar` | ALUNO (dono) | 200, 403, 404, 409 |
| GET | `/api/encomendas/{id}` | dono ou bar | 200, 403, 404 |
| GET | `/api/encomendas?data=&intervaloId=` | FUNCIONARIO, GESTOR | 200 |
| PUT | `/api/encomendas/{id}/estado` | FUNCIONARIO, GESTOR | 200, 404, 409 |
| GET | `/api/alunos` | GESTOR | 200 |
| POST | `/api/alunos/{numero}/carregamentos` | GESTOR | 200, 400, 404 |
| GET | `/api/vendas?data=aaaa-mm-dd` | GESTOR | 200, 400 |

Erros no formato `{"status": 409, "codigo": "SALDO_INSUFICIENTE", "mensagem": "...", "detalhes": []}`.

- Coleção Postman: [docs/postman/barescola.postman_collection.json](docs/postman/barescola.postman_collection.json)
- SOAP `CartaoWS`: [docs/soap/README.md](docs/soap/README.md) · WSDL em `http://localhost:8080/barescola/CartaoWS?wsdl`
- WebSockets: `ws://localhost:8080/barescola/ws/aluno` (avisos ao aluno) e `/ws/balcao` (fila)

## Documentação (`docs/`)

| Documento | Ficheiro |
|---|---|
| Diagrama entidade-relação | [docs/diagramas/entidade-relacao.md](docs/diagramas/entidade-relacao.md) |
| Diagrama de estados da encomenda | [docs/diagramas/estados-encomenda.md](docs/diagramas/estados-encomenda.md) |
| Diagrama de camadas | [docs/diagramas/arquitetura.md](docs/diagramas/arquitetura.md) |
| Coleção Postman | [docs/postman/](docs/postman/) |
| Projeto SoapUI + WSDL | [docs/soap/](docs/soap/) |
| Manual de utilizador | [docs/manual-utilizador.md](docs/manual-utilizador.md) |
| Checklist de segurança | [docs/checklist-seguranca.md](docs/checklist-seguranca.md) |
| Plano por etapas, Git e Kanban | [docs/plano-etapas.md](docs/plano-etapas.md) |
| Guião do Demo Day | [docs/demo-day.md](docs/demo-day.md) |

## Progresso por etapa

| Etapa | Tag | Estado |
|-------|-----|--------|
| 1 Jakarta EE | etapa-1 | ✅ |
| 2 HTML/CSS/JS | etapa-2 | ✅ |
| 3 Servlets/JSP | etapa-3 | ✅ |
| 4 JPA | etapa-4 | ✅ |
| 5 EJB | etapa-5 | ✅ |
| 6 JAX-RS | etapa-6 | ✅ |
| 7 JSF/WebSockets | etapa-7 | ✅ |
| 8 JMS | etapa-8 | ✅ |
| 9 SOAP | etapa-9 | ✅ |
| 10 Segurança | etapa-10 | ✅ |

## Uso de IA

O que pedimos, o que aproveitámos, o que tivemos de corrigir.

- **O que pedimos:** estrutura inicial do projeto a partir do guia (código das 10 etapas, documentação e configuração do VS Code).
- **O que aproveitámos:** _(preencher pela equipa)_
- **O que tivemos de corrigir:** _(preencher pela equipa – ex.: erros de compilação, ajustes ao WildFly, testes no SoapUI)_
