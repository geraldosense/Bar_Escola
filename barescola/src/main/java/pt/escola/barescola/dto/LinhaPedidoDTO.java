package pt.escola.barescola.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Uma linha do carrinho enviada pelo cliente: produto + quantidade. */
public record LinhaPedidoDTO(@NotNull Long produtoId, @Min(1) @Max(20) int quantidade) implements Serializable {
}
