package pt.escola.barescola.rest;

import java.time.LocalDateTime;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.escola.barescola.dto.EstadoAppDTO;

/** Etapa 1 – GET /api/estado: confirma que a aplicação está no ar (público). */
@Path("estado")
@Produces(MediaType.APPLICATION_JSON)
public class EstadoResource {

    @GET
    @PermitAll
    public EstadoAppDTO estado() {
        return new EstadoAppDTO("BarEscola", "1.0.0", "OK", LocalDateTime.now());
    }
}
