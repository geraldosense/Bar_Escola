package pt.escola.barescola.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.UtilizadorDTO;
import pt.escola.barescola.service.AlunoService;

/** GET /api/me – quem está autenticado, o papel e (se for aluno) o saldo. */
@Path("me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    @EJB
    private AlunoService alunos;

    @GET
    @RolesAllowed({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
    public UtilizadorDTO me(@Context SecurityContext sc) {
        String nome = sc.getUserPrincipal().getName();
        String papel = sc.isUserInRole(Papeis.GESTOR) ? Papeis.GESTOR
                : sc.isUserInRole(Papeis.FUNCIONARIO) ? Papeis.FUNCIONARIO
                : Papeis.ALUNO;
        return new UtilizadorDTO(nome, papel,
                Papeis.ALUNO.equals(papel) ? alunos.porEmail(nome).orElse(null) : null);
    }
}
