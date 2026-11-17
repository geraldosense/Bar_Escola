package pt.escola.barescola.soap;

import jakarta.ejb.EJB;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import pt.escola.barescola.service.AlunoService;

/**
 * Etapa 9 – serviço SOAP para o sistema de cartões da escola.
 * WSDL: http://localhost:8080/barescola/CartaoWS?wsdl
 */
@WebService(serviceName = "CartaoWS", portName = "CartaoPort", targetNamespace = CartaoWS.NAMESPACE)
public class CartaoWS {

    public static final String NAMESPACE = "http://soap.barescola.escola.pt/";

    @EJB
    private AlunoService alunos;

    @WebMethod(operationName = "saldo")
    @WebResult(name = "saldoCartao")
    public SaldoCartao saldo(@WebParam(name = "numeroCartao") int numeroCartao) throws CartaoInvalidoException {
        if (numeroCartao <= 0) {
            throw new CartaoInvalidoException("Número de cartão inválido: " + numeroCartao);
        }
        return alunos.porNumeroCartao(numeroCartao)
                .map(a -> new SaldoCartao(a.numero(), a.nome(), a.turma(), a.saldo()))
                .orElseThrow(() -> new CartaoInvalidoException("Cartão " + numeroCartao + " não existe."));
    }
}
