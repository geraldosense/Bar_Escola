package pt.escola.barescola.service.exception;

import java.time.LocalTime;

/** RN3 – as encomendas para este intervalo já fecharam. */
public class EncomendasFechadasException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public EncomendasFechadasException(String intervalo, LocalTime fecho) {
        super("ENCOMENDAS_FECHADAS", 409,
                "As encomendas para \"" + intervalo + "\" fecharam às " + fecho + ".");
    }
}
