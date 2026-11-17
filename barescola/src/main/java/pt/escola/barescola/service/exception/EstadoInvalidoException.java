package pt.escola.barescola.service.exception;

/** Transição de estado não permitida (inclui RN4 – cancelar fora do estado PAGA). */
public class EstadoInvalidoException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public EstadoInvalidoException(String mensagem) {
        super("ESTADO_INVALIDO", 409, mensagem);
    }
}
