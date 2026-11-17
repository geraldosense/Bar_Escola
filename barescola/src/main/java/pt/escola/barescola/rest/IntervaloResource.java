package pt.escola.barescola.rest;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.IntervaloDTO;
import pt.escola.barescola.service.IntervaloService;

/** GET /api/intervalos – intervalos do dia e se ainda aceitam encomendas (RN3). */
@Path("intervalos")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
public class IntervaloResource {

    @EJB
    private IntervaloService intervalos;

    @GET
    public List<IntervaloDTO> listar() {
        return intervalos.listar();
    }
}
