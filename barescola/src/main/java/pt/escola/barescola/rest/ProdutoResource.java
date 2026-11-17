package pt.escola.barescola.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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
import pt.escola.barescola.dto.CategoriaDTO;
import pt.escola.barescola.dto.ProdutoDTO;
import pt.escola.barescola.service.ProdutoService;

/**
 * CRUD de produtos.
 * <pre>
 * GET    /api/produtos?ativos=true   menu do dia (todos os papéis)
 * GET    /api/produtos/{id}
 * POST   /api/produtos               201 Created + Location   (GESTOR)
 * PUT    /api/produtos/{id}          200                      (GESTOR)
 * PATCH  /api/produtos/{id}/stock    {"stock": 10}            (GESTOR)
 * DELETE /api/produtos/{id}          204 apagado | 200 desativado (GESTOR)
 * GET    /api/produtos/categorias
 * </pre>
 */
@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
public class ProdutoResource {

    @EJB
    private ProdutoService produtos;

    @Context
    private UriInfo uriInfo;

    @GET
    public List<ProdutoDTO> listar(@QueryParam("ativos") @DefaultValue("true") boolean ativos) {
        return produtos.listar(ativos);
    }

    @GET
    @Path("categorias")
    public List<CategoriaDTO> categorias() {
        return produtos.categorias();
    }

    @GET
    @Path("{id}")
    public ProdutoDTO obter(@PathParam("id") Long id) {
        return produtos.obter(id);
    }

    @POST
    @RolesAllowed(Papeis.GESTOR)
    public Response criar(@Valid @NotNull ProdutoDTO dados) {
        ProdutoDTO criado = produtos.criar(dados);
        URI local = uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id())).build();
        return Response.created(local).entity(criado).build();
    }

    @PUT
    @Path("{id}")
    @RolesAllowed(Papeis.GESTOR)
    public ProdutoDTO atualizar(@PathParam("id") Long id, @Valid @NotNull ProdutoDTO dados) {
        return produtos.atualizar(id, dados);
    }

    @PATCH
    @Path("{id}/stock")
    @RolesAllowed(Papeis.GESTOR)
    public ProdutoDTO atualizarStock(@PathParam("id") Long id, @NotNull Map<String, Integer> corpo) {
        Integer stock = corpo.get("stock");
        if (stock == null) {
            throw new jakarta.ws.rs.BadRequestException("Indique {\"stock\": n}");
        }
        return produtos.atualizarStock(id, stock);
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed(Papeis.GESTOR)
    public Response remover(@PathParam("id") Long id) {
        boolean apagado = produtos.remover(id);
        return apagado
                ? Response.noContent().build()
                : Response.ok(Map.of("mensagem", "O produto já tem vendas: foi desativado em vez de apagado.")).build();
    }
}
