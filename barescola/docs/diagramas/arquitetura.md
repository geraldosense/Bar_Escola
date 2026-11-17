# Diagrama de camadas

```mermaid
flowchart TB
    subgraph Apresentacao["Apresentação"]
        HTML["HTML/CSS/JS<br/>aluno/menu.html · aluno/pedidos.html · balcao/fila.html"]
        JSP["Servlets + JSP<br/>/menu · /carrinho · /encomendar"]
        JSF["JSF (Faces)<br/>backoffice/*.xhtml"]
    end

    subgraph API["API"]
        REST["JAX-RS<br/>/api/produtos · /api/encomendas · /api/vendas …"]
        WS["WebSockets<br/>/ws/aluno · /ws/balcao"]
        SOAP["JAX-WS (SOAP)<br/>CartaoWS"]
    end

    subgraph Negocio["Negócio"]
        EJB["EJB @Stateless<br/>EncomendaService · ProdutoService · AlunoService · VendasService"]
        TIMER["@Schedule<br/>FechoDiaTimer (RN5)"]
        JMS["JMS<br/>CozinhaQueue → CozinhaMDB"]
        CDI["Eventos CDI<br/>AFTER_SUCCESS → Notificador"]
    end

    subgraph Persistencia["Persistência"]
        JPA["JPA / Hibernate<br/>6 entidades"]
    end

    DB[("H2<br/>BarEscolaDS")]

    HTML -- fetch JSON --> REST
    HTML -. WebSocket .-> WS
    JSP --> EJB
    JSF --> EJB
    REST --> EJB
    SOAP --> EJB
    TIMER --> EJB
    EJB -- envia --> JMS
    JMS -- consome --> EJB
    EJB -- dispara --> CDI
    CDI --> WS
    EJB --> JPA --> DB
```

## Pacotes

| Pacote | Camada | Conteúdo |
|---|---|---|
| `pt.escola.barescola.model` | Persistência | Entidades JPA + enum de estados |
| `pt.escola.barescola.dto` | API | Records de entrada/saída (nunca se expõem entidades) |
| `pt.escola.barescola.service` | Negócio | EJB com as regras RN1–RN5 e exceções de negócio |
| `pt.escola.barescola.jms` | Negócio | Produtor e MDB da fila da cozinha |
| `pt.escola.barescola.timer` | Negócio | Timer de fecho do dia |
| `pt.escola.barescola.rest` | API | Recursos JAX-RS e *exception mappers* |
| `pt.escola.barescola.websocket` | API | Endpoints e notificador WebSocket |
| `pt.escola.barescola.soap` | API | Serviço SOAP `CartaoWS` |
| `pt.escola.barescola.web` | Apresentação | Servlets, carrinho de sessão, filtro de segurança |
| `pt.escola.barescola.jsf` | Apresentação | *Backing beans* do backoffice |
| `pt.escola.barescola.config` | Transversal | JAX-RS, papéis, relógio, dados iniciais |
