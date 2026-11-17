package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

/** Resposta ao pagamento: a encomenda criada e o saldo que sobrou no cartão (US03). */
public record PagamentoDTO(EncomendaDTO encomenda, BigDecimal saldoRestante) implements Serializable {
}
