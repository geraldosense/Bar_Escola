package pt.escola.barescola.rest.mapper;

import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pt.escola.barescola.dto.ErroDTO;

/** Bean Validation (@Valid, @NotNull, @Min...) → 400 Bad Request com a lista de campos inválidos. */
@Provider
public class ValidacaoExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(paraErro(e))
                .build();
    }

    static ErroDTO paraErro(ConstraintViolationException e) {
        List<String> detalhes = e.getConstraintViolations().stream()
                .map(ValidacaoExceptionMapper::descrever)
                .sorted()
                .toList();
        return new ErroDTO(400, "DADOS_INVALIDOS", "Os dados enviados não são válidos.", detalhes);
    }

    private static String descrever(ConstraintViolation<?> v) {
        String caminho = v.getPropertyPath().toString();
        String campo = caminho.contains(".") ? caminho.substring(caminho.lastIndexOf('.') + 1) : caminho;
        return campo + ": " + v.getMessage();
    }
}
