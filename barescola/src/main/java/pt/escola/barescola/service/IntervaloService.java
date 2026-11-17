package pt.escola.barescola.service;

import java.time.LocalTime;
import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Relogio;
import pt.escola.barescola.dto.IntervaloDTO;
import pt.escola.barescola.model.Intervalo;

@Stateless
@PermitAll
public class IntervaloService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Relogio relogio;

    /** Intervalos do dia, com a indicação de quais ainda aceitam encomendas (RN3). */
    public List<IntervaloDTO> listar() {
        LocalTime agora = relogio.horaAtual();
        boolean demo = relogio.modoDemo();
        return em.createNamedQuery(Intervalo.TODOS, Intervalo.class).getResultList().stream()
                .map(i -> IntervaloDTO.from(i, demo || i.aceitaEncomendas(agora)))
                .toList();
    }
}
