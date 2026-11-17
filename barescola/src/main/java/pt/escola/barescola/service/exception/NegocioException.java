package pt.escola.barescola.service.exception;

import jakarta.ejb.ApplicationException;

/**
 * Violação de uma regra de negócio.
 *
 * <p>{@code rollback = true}: quando esta exceção sai de um método EJB, o contentor desfaz
 * <strong>toda</strong> a transação (saldo, stock e encomenda) — é isto que garante o
 * "tudo ou nada" do pagamento.</p>
 */
@ApplicationException(rollback = true, inherited = true)
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final int status;

    public NegocioException(String codigo, int status, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Código HTTP sugerido para a API REST. */
    public int getStatus() {
        return status;
    }
}
