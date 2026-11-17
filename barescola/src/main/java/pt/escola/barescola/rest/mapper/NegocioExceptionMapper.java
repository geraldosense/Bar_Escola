package pt.escola.barescola.rest.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pt.escola.barescola.dto.ErroDTO;
import pt.escola.barescola.service.exception.NegocioException;

/** Regras de negócio violadas → 400/403/404/409 com um JSON de erro legível. */
@Provider
public class NegocioExceptionMapper implements ExceptionMapper<NegocioException> {

    @Override
    public Response toResponse(NegocioException e) {
        return Response.status(e.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroDTO(e.getStatus(), e.getCodigo(), e.getMessage()))
                .build();
    }
}
