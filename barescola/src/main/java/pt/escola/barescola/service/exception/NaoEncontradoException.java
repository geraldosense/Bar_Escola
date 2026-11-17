package pt.escola.barescola.service.exception;

public class NaoEncontradoException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public NaoEncontradoException(String recurso, Object id) {
        super("NAO_ENCONTRADO", 404, recurso + " " + id + " não encontrado(a).");
    }
}
