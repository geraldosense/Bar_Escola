# Guião do Demo Day (10 min)

## Preparação (antes de entrar na sala)

```bash
# 1. Permitir encomendas a qualquer hora (desliga a RN3 só para a demo)
export BARESCOLA_MODO_DEMO=true          # Windows PowerShell: $env:BARESCOLA_MODO_DEMO="true"

# 2. Aceitar ligações de outros dispositivos na mesma rede
$WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0
```

- Descobrir o IP do portátil (`ipconfig` / `ip a`) e gerar um QR code para `http://<IP>:8080/barescola`.
- Ter três separadores abertos: aluno (telemóvel), balcão (projetor), gestor.
- Carregar saldo no `aluno3@barescola.pt` para a pessoa do público (ou criar mais um utilizador com `add-user`).

## Sequência

| Min | O quê | Mostra |
|---|---|---|
| 0–1 | Problema: fila de 15 min no intervalo | Contexto |
| 1–3 | Alguém do público lê o QR, entra como aluno e encomenda | US01–US03, saldo restante |
| 3–4 | No projetor, o pedido aparece no balcão e passa a *Em preparação* sozinho | JMS + WebSocket |
| 4–5 | Funcionário marca *Pronta* → o telemóvel do público vibra | US04, WebSocket |
| 5–6 | Tentar encomendar a última sandes de frango em dois telemóveis | RN2 + transação |
| 6–7 | `aluno2` (2 €) tenta pagar uma tosta mista ×3 → erro; saldo intacto | RN1 + rollback |
| 7–8 | Backoffice: stock, vendas do dia, fecho do dia | US06, US07, RN5 |
| 8–9 | SoapUI: cartão válido e inválido | Etapa 9 |
| 9–10 | Arquitetura em camadas e checklist de segurança | Etapas 1 e 10 |
