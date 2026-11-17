package pt.escola.barescola.rest;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.AlunoDTO;
import pt.escola.barescola.dto.CarregamentoDTO;
import pt.escola.barescola.service.AlunoService;

/** Gestão de alunos e carregamentos de saldo (GESTOR). */
@Path("alunos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Papeis.GESTOR)
public class AlunoResource {

    @EJB
    private AlunoService alunos;

    @GET
    public List<AlunoDTO> listar() {
        return alunos.listar();
    }

    @POST
    @Path("{numero}/carregamentos")
    public AlunoDTO carregar(@PathParam("numero") int numero, @Valid @NotNull CarregamentoDTO carregamento) {
        return alunos.carregarSaldo(numero, carregamento.valor());
    }
}
