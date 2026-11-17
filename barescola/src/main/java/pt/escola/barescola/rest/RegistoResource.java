package pt.escola.barescola.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.escola.barescola.dto.AlunoDTO;
import pt.escola.barescola.dto.RegistoDTO;
import pt.escola.barescola.service.RegistoService;

/** POST /api/registo – criar conta de aluno (público). Responde 201, 400 ou 409. */
@Path("registo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistoResource {

    @EJB
    private RegistoService registo;

    @POST
    @PermitAll
    public Response registar(@Valid @NotNull RegistoDTO dados) {
        AlunoDTO aluno = registo.registar(dados);
        return Response.status(Response.Status.CREATED).entity(aluno).build();
    }
}
