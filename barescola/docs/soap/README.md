# Serviço SOAP – CartaoWS (etapa 9)

Consulta do saldo de um cartão de estudante, para o sistema de cartões da escola.

| | |
|---|---|
| Endpoint | `http://localhost:8080/barescola/CartaoWS` |
| WSDL | `http://localhost:8080/barescola/CartaoWS?wsdl` |
| Operação | `saldo(numeroCartao: int) → saldoCartao {numeroCartao, nome, turma, saldo}` |
| Erro | SOAP Fault `CartaoInvalido` quando o cartão não existe ou o número é ≤ 0 |

## Testar no SoapUI

1. **File → New SOAP Project**, *Initial WSDL* = `http://localhost:8080/barescola/CartaoWS?wsdl`.
2. Abra `CartaoWSServiceSoapBinding → saldo → Request 1` e cole o conteúdo de:
   - [`pedido-valido.xml`](pedido-valido.xml) – cartão 1001 (Ana Silva) → devolve o saldo;
   - [`pedido-invalido.xml`](pedido-invalido.xml) – cartão 9999 → devolve `soap:Fault`.
3. Crie um **TestSuite** com os dois pedidos e adicione as *assertions*:
   - válido: *SOAP Response*, *Not SOAP Fault*, *Contains* `Ana Silva`;
   - inválido: *SOAP Fault*, *Contains* `não existe`.
4. **File → Save Project As…** → `docs/soap/BarEscola-soapui-project.xml` (é este ficheiro que se entrega).

## Testar com curl

```bash
curl -s -H "Content-Type: text/xml" -d @docs/soap/pedido-valido.xml http://localhost:8080/barescola/CartaoWS
curl -s -H "Content-Type: text/xml" -d @docs/soap/pedido-invalido.xml http://localhost:8080/barescola/CartaoWS
```
