package pt.escola.barescola.model;

import java.util.Set;

/**
 * Ciclo de vida de uma encomenda.
 *
 * <pre>
 * PAGA → EM_PREPARACAO → PRONTA → ENTREGUE
 *   └──→ CANCELADA (só a partir de PAGA – RN4)
 * </pre>
 */
public enum EstadoEncomenda {
    PAGA,
    EM_PREPARACAO,
    PRONTA,
    ENTREGUE,
    CANCELADA;

    /** Estados que ainda aparecem na fila do balcão. */
    public static final Set<EstadoEncomenda> ATIVOS = Set.of(PAGA, EM_PREPARACAO, PRONTA);

    /** Indica se a transição {@code this → destino} é permitida. */
    public boolean podeMudarPara(EstadoEncomenda destino) {
        return switch (this) {
            case PAGA -> destino == EM_PREPARACAO || destino == CANCELADA;
            case EM_PREPARACAO -> destino == PRONTA;
            case PRONTA -> destino == ENTREGUE;
            case ENTREGUE, CANCELADA -> false;
        };
    }

    /** RN4 – só se cancela enquanto estiver PAGA. */
    public boolean podeCancelar() {
        return this == PAGA;
    }
}
