package pt.escola.barescola.rest;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.CriarEncomendaDTO;
import pt.escola.barescola.dto.EncomendaDTO;
import pt.escola.barescola.dto.MudarEstadoDTO;
import pt.escola.barescola.dto.PagamentoDTO;
import pt.escola.barescola.service.EncomendaService;

/**
 * Operações de encomenda.
 * <pre>
 * POST /api/encomendas                    criar + pagar      201 (ALUNO)
 * GET  /api/encomendas/minhas             os meus pedidos    200 (ALUNO)
 * POST /api/encomendas/{id}/cancelar      RN4                200 (ALUNO)
 * GET  /api/encomendas/{id}               detalhe            200 (dono ou bar)
 * GET  /api/encomendas?data=&amp;intervaloId= fila do balcão     200 (FUNCIONARIO, GESTOR)
 * PUT  /api/encomendas/{id}/estado        {"estado":"PRONTA"} 200 (FUNCIONARIO, GESTOR)
 * </pre>
 */
@Path("encomendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncomendaResource {

    @EJB
    private EncomendaService encomendas;

    @Context
    private UriInfo uriInfo;

    @POST
    @RolesAllowed(Papeis.ALUNO)
    public Response criar(@Valid @NotNull CriarEncomendaDTO pedido) {
        PagamentoDTO pagamento = encomendas.pagar(pedido);
        URI local = uriInfo.getAbsolutePathBuilder().path(String.valueOf(pagamento.encomenda().id())).build();
        return Response.created(local).entity(pagamento).build();
    }

    @GET
    @Path("minhas")
    @RolesAllowed(Papeis.ALUNO)
    public List<EncomendaDTO> minhas() {
        return encomendas.minhas();
    }

    @POST
    @Path("{id}/cancelar")
    @RolesAllowed(Papeis.ALUNO)
    public EncomendaDTO cancelar(@PathParam("id") Long id) {
        return encomendas.cancelar(id);
    }

    @GET
    @Path("{id}")
    @RolesAllowed({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
    public EncomendaDTO obter(@PathParam("id") Long id) {
        return encomendas.obter(id);
    }

    @GET
    @RolesAllowed({Papeis.FUNCIONARIO, Papeis.GESTOR})
    public List<EncomendaDTO> fila(@QueryParam("data") LocalDate data, @QueryParam("intervaloId") Long intervaloId) {
        return encomendas.fila(data, intervaloId);
    }

    @PUT
    @Path("{id}/estado")
    @RolesAllowed({Papeis.FUNCIONARIO, Papeis.GESTOR})
    public EncomendaDTO mudarEstado(@PathParam("id") Long id, @Valid @NotNull MudarEstadoDTO corpo) {
        return encomendas.mudarEstado(id, corpo.estado());
    }
}
