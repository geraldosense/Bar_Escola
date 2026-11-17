package pt.escola.barescola.rest.mapper;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pt.escola.barescola.dto.ErroDTO;

/**
 * Exceções de sistema vindas dos EJB: desembrulha a causa e escolhe o código HTTP.
 * Nunca envia stack traces ao cliente (checklist de segurança).
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    private static final Logger LOG = Logger.getLogger(EJBExceptionMapper.class.getName());

    @Override
    public Response toResponse(EJBException e) {
        if (e instanceof EJBAccessException) {
            return erro(new ErroDTO(403, "ACESSO_NEGADO", "Não tem permissão para esta operação."));
        }
        Throwable causa = e;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                return erro(ValidacaoExceptionMapper.paraErro(cve));
            }
            if (causa instanceof OptimisticLockException
                    || causa.getClass().getSimpleName().contains("OptimisticLock")
                    || causa.getClass().getSimpleName().contains("StaleObjectState")) {
                return erro(new ErroDTO(409, "CONFLITO",
                        "Os dados foram alterados por outra pessoa entretanto. Tente novamente."));
            }
            causa = causa.getCause();
        }
        LOG.log(Level.SEVERE, "Erro inesperado na API", e);
        return erro(new ErroDTO(500, "ERRO_INTERNO", "Ocorreu um erro inesperado."));
    }

    private static Response erro(ErroDTO erro) {
        return Response.status(erro.status()).type(MediaType.APPLICATION_JSON).entity(erro).build();
    }
}
