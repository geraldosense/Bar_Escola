package pt.escola.barescola.soap;

import jakarta.xml.ws.WebFault;

/** SOAP Fault devolvido quando o número de cartão não existe. */
@WebFault(name = "CartaoInvalido", targetNamespace = CartaoWS.NAMESPACE)
public class CartaoInvalidoException extends Exception {

    private static final long serialVersionUID = 1L;

    public CartaoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
