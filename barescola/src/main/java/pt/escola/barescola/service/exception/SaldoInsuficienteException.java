package pt.escola.barescola.service.exception;

import java.math.BigDecimal;

/** RN1 – o saldo do cartão não cobre o total. */
public class SaldoInsuficienteException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal total) {
        super("SALDO_INSUFICIENTE", 409,
                "Saldo insuficiente: tens " + saldo + " € e a encomenda custa " + total + " €.");
    }
}
