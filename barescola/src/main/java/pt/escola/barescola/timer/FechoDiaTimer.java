package pt.escola.barescola.timer;

import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import pt.escola.barescola.config.Relogio;
import pt.escola.barescola.service.EncomendaService;

/** RN5 – todos os dias às 23:30 as encomendas PRONTA não levantadas passam a ENTREGUE. */
@Singleton
public class FechoDiaTimer {

    private static final Logger LOG = Logger.getLogger(FechoDiaTimer.class.getName());

    @EJB
    private EncomendaService encomendas;

    @Inject
    private Relogio relogio;

    @Schedule(hour = "23", minute = "30", persistent = false, info = "Fecho do dia BarEscola")
    public void fecharDia() {
        LOG.info("Timer de fecho do dia a correr...");
        encomendas.fecharDia(relogio.hoje());
    }
}
