package pt.escola.barescola.service.exception;

public class DadosInvalidosException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super("DADOS_INVALIDOS", 400, mensagem);
    }
}
