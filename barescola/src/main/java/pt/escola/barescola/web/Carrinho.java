package pt.escola.barescola.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.escola.barescola.dto.LinhaPedidoDTO;

/**
 * Etapa 3 – carrinho guardado na {@code HttpSession}.
 * Guarda nome e preço apenas para mostrar; o preço que conta é sempre o da BD no momento do pagamento.
 */
public class Carrinho implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Nome do atributo de sessão. */
    public static final String ATRIBUTO = "carrinho";

    public static final int MAX_POR_PRODUTO = 20;

    private final Map<Long, Item> itens = new LinkedHashMap<>();

    public void adicionar(Long produtoId, String nome, BigDecimal preco) {
        itens.compute(produtoId, (id, item) -> {
            if (item == null) {
                return new Item(id, nome, preco, 1);
            }
            item.quantidade = Math.min(item.quantidade + 1, MAX_POR_PRODUTO);
            return item;
        });
    }

    /** Tira uma unidade; quando chega a zero, remove a linha. */
    public void remover(Long produtoId) {
        itens.computeIfPresent(produtoId, (id, item) -> --item.quantidade > 0 ? item : null);
    }

    public void limpar() {
        itens.clear();
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }

    public List<Item> getItens() {
        return new ArrayList<>(itens.values());
    }

    public int getNumeroArtigos() {
        return itens.values().stream().mapToInt(Item::getQuantidade).sum();
    }

    public BigDecimal getTotal() {
        return itens.values().stream()
                .map(Item::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<LinhaPedidoDTO> paraLinhas() {
        return itens.values().stream()
                .map(i -> new LinhaPedidoDTO(i.produtoId, i.quantidade))
                .toList();
    }

    /** Linha do carrinho (JavaBean, para ser lida facilmente na JSP). */
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Long produtoId;
        private final String nome;
        private final BigDecimal preco;
        private int quantidade;

        Item(Long produtoId, String nome, BigDecimal preco, int quantidade) {
            this.produtoId = produtoId;
            this.nome = nome;
            this.preco = preco;
            this.quantidade = quantidade;
        }

        public Long getProdutoId() { return produtoId; }

        public String getNome() { return nome; }

        public BigDecimal getPreco() { return preco; }

        public int getQuantidade() { return quantidade; }

        public BigDecimal getSubtotal() {
            return preco.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    /** Obtém (ou cria) o carrinho da sessão. */
    public static Carrinho daSessao(jakarta.servlet.http.HttpSession sessao) {
        Carrinho c = (Carrinho) sessao.getAttribute(ATRIBUTO);
        if (c == null) {
            c = new Carrinho();
            sessao.setAttribute(ATRIBUTO, c);
        }
        return c;
    }
}
