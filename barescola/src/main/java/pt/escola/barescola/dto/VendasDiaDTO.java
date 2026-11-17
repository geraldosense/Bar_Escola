package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Vendas de um dia (US07). */
public record VendasDiaDTO(LocalDate data, long numeroEncomendas, BigDecimal total, List<VendaProdutoDTO> produtos) implements Serializable {
}
