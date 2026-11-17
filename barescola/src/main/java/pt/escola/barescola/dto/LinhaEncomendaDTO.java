package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

import pt.escola.barescola.model.LinhaEncomenda;

public record LinhaEncomendaDTO(Long produtoId, String produto, int quantidade, BigDecimal precoUnitario,
        BigDecimal subtotal) implements Serializable {

    public static LinhaEncomendaDTO from(LinhaEncomenda l) {
        return new LinhaEncomendaDTO(l.getProduto().getId(), l.getProduto().getNome(), l.getQuantidade(),
                l.getPrecoUnitario(), l.getSubtotal());
    }
}
