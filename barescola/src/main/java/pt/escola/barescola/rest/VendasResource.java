package pt.escola.barescola.rest;

import java.time.LocalDate;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.VendasDiaDTO;
import pt.escola.barescola.service.VendasService;

/** GET /api/vendas?data=2026-11-17 – vendas do dia (US07). Sem data = hoje. */
@Path("vendas")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Papeis.GESTOR)
public class VendasResource {

    @EJB
    private VendasService vendas;

    @GET
    public VendasDiaDTO vendasDoDia(@QueryParam("data") LocalDate data) {
        return vendas.vendasDoDia(data != null ? data : LocalDate.now());
    }
}
