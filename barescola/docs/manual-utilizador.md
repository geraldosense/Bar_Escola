# Manual de utilizador – BarEscola

Endereço: **http://localhost:8080/barescola** (no Demo Day, o IP do portátil, ex.: `http://192.168.1.20:8080/barescola`).

Ao entrar, cada pessoa é enviada para o seu ecrã conforme o papel.

---

## 1. Aluno

### Encomendar
1. Entre com o seu email e palavra-passe.
2. No **Menu do dia** veja os produtos, preços e quantos há. Os esgotados aparecem a cinzento.
3. Toque em **+** para juntar ao carrinho. No telemóvel, a barra verde em baixo mostra o total; toque em **Ver carrinho**.
4. No carrinho, ajuste quantidades com **−** e **+**.
5. Escolha o **intervalo** em que vai levantar. Os intervalos cujo prazo já passou aparecem como *fechado* (as encomendas fecham 15 minutos antes).
6. Toque em **Pagar com o cartão**. O valor é descontado do saldo e o novo saldo aparece no topo.

Se aparecer uma mensagem vermelha:
- *Saldo insuficiente* – peça um carregamento no bar.
- *Stock insuficiente* – alguém comprou o último entretanto; retire o produto.
- *Encomendas fechadas* – escolha outro intervalo.

### Acompanhar e cancelar
1. Abra **Os meus pedidos**. Cada encomenda mostra o estado e uma barra de progresso:
   **Paga → Em preparação → Pronta → Entregue**.
2. Quando o bar marcar o pedido como pronto, aparece um aviso 🔔 (e o telemóvel vibra) – não é preciso recarregar a página.
3. Enquanto o estado for **Paga** pode tocar em **Cancelar**: o dinheiro volta para o cartão.

## 2. Funcionário do balcão

1. Entre com a conta do balcão – abre o **Ecrã do balcão**.
2. À esquerda estão as encomendas por preparar, por intervalo e por ordem de chegada; ao centro, as prontas a levantar.
3. Use os botões de cada cartão: **Começar a preparar** → **Marcar pronta** (o aluno é avisado) → **Entregue**.
4. Pode filtrar por intervalo. O ecrã atualiza-se sozinho quando chegam encomendas novas.

## 3. Gestor

Menu **Gestão** (backoffice):

- **Produtos e stock** – criar/editar produtos, mudar preço e categoria, ajustar stock com **−/+/+10**, esconder do menu (*Desativar*) ou remover. Um produto que já foi vendido é desativado em vez de apagado.
- **Vendas** – escolha o dia e veja o total vendido, o número de encomendas e as vendas por produto. O botão **Executar fecho do dia** passa as encomendas *Prontas* não levantadas para *Entregue* (o servidor faz isto automaticamente às 23:30).
- **Saldos** – carregar o cartão de um aluno (0,50 € a 100 €).
- **Balcão** – o gestor também pode usar o ecrã do balcão.

## Utilizadores de demonstração

| Papel | Email | Palavra-passe |
|---|---|---|
| Aluno (15 €) | aluno1@barescola.pt | Aluno#2026 |
| Aluno (2 €) | aluno2@barescola.pt | Aluno#2026 |
| Aluno (8,50 €) | aluno3@barescola.pt | Aluno#2026 |
| Funcionário | balcao@barescola.pt | Balcao#2026 |
| Gestor | gestor@barescola.pt | Gestor#2026 |
