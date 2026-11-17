package pt.escola.barescola.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.VendaProdutoDTO;
import pt.escola.barescola.dto.VendasDiaDTO;
import pt.escola.barescola.model.EstadoEncomenda;

/** US07 – vendas do dia (consulta JPQL com SUM por produto e parâmetro de data). */
@Stateless
@RolesAllowed(Papeis.GESTOR)
public class VendasService {

    private static final String VENDAS_POR_PRODUTO = """
            SELECT new pt.escola.barescola.dto.VendaProdutoDTO(
                       p.nome, SUM(l.quantidade), SUM(l.precoUnitario * l.quantidade))
            FROM LinhaEncomenda l
                 JOIN l.produto p
                 JOIN l.encomenda e
            WHERE e.dataHora >= :inicio AND e.dataHora < :fim
              AND e.estado <> :cancelada
            GROUP BY p.nome
            """;

    private static final String NUMERO_ENCOMENDAS = """
            SELECT COUNT(e) FROM Encomenda e
            WHERE e.dataHora >= :inicio AND e.dataHora < :fim
              AND e.estado <> :cancelada
            """;

    @PersistenceContext
    private EntityManager em;

    public VendasDiaDTO vendasDoDia(LocalDate dia) {
        List<VendaProdutoDTO> produtos = em.createQuery(VENDAS_POR_PRODUTO, VendaProdutoDTO.class)
                .setParameter("inicio", dia.atStartOfDay())
                .setParameter("fim", dia.plusDays(1).atStartOfDay())
                .setParameter("cancelada", EstadoEncomenda.CANCELADA)
                .getResultList().stream()
                // ordenado em Java: a especificação JPA não permite ORDER BY SUM(...) dentro de "SELECT new"
                .sorted(java.util.Comparator.comparing(VendaProdutoDTO::total).reversed())
                .toList();

        long numero = em.createQuery(NUMERO_ENCOMENDAS, Long.class)
                .setParameter("inicio", dia.atStartOfDay())
                .setParameter("fim", dia.plusDays(1).atStartOfDay())
                .setParameter("cancelada", EstadoEncomenda.CANCELADA)
                .getSingleResult();

        BigDecimal total = produtos.stream()
                .map(VendaProdutoDTO::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new VendasDiaDTO(dia, numero, total, produtos);
    }
}
