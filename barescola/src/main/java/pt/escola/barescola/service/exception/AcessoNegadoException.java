package pt.escola.barescola.service.exception;

/** Um aluno tentou ver ou cancelar uma encomenda que não é sua. */
public class AcessoNegadoException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super("ACESSO_NEGADO", 403, mensagem);
    }
}
