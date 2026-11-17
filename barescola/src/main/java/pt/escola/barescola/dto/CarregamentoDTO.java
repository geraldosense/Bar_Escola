package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Carregamento de saldo feito pelo gestor. */
public record CarregamentoDTO(@NotNull @DecimalMin("0.50") @DecimalMax("100.00") BigDecimal valor) implements Serializable {
}
