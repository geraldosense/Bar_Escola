package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import pt.escola.barescola.model.Produto;

/** Produto como é exposto pela API (entrada e saída). */
public record ProdutoDTO(
        Long id,
        @NotBlank @Size(max = 80) String nome,
        @NotNull @DecimalMin("0.01") @Digits(integer = 4, fraction = 2) BigDecimal preco,
        @NotNull @PositiveOrZero Integer stock,
        boolean ativo,
        @NotNull Long categoriaId,
        String categoria,
        boolean disponivel) implements Serializable {

    public static ProdutoDTO from(Produto p) {
        return new ProdutoDTO(p.getId(), p.getNome(), p.getPreco(), p.getStock(), p.isAtivo(),
                p.getCategoria().getId(), p.getCategoria().getNome(), p.isDisponivel());
    }
}
