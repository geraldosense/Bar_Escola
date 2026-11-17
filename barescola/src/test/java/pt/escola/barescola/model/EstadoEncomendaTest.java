package pt.escola.barescola.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoEncomendaTest {

    @Test
    @DisplayName("Fluxo normal PAGA → EM_PREPARACAO → PRONTA → ENTREGUE")
    void fluxoNormal() {
        assertTrue(EstadoEncomenda.PAGA.podeMudarPara(EstadoEncomenda.EM_PREPARACAO));
        assertTrue(EstadoEncomenda.EM_PREPARACAO.podeMudarPara(EstadoEncomenda.PRONTA));
        assertTrue(EstadoEncomenda.PRONTA.podeMudarPara(EstadoEncomenda.ENTREGUE));
    }

    @Test
    @DisplayName("RN4 – só se cancela em PAGA")
    void cancelamento() {
        assertTrue(EstadoEncomenda.PAGA.podeCancelar());
        assertFalse(EstadoEncomenda.EM_PREPARACAO.podeCancelar());
        assertFalse(EstadoEncomenda.PRONTA.podeCancelar());
        assertFalse(EstadoEncomenda.EM_PREPARACAO.podeMudarPara(EstadoEncomenda.CANCELADA));
    }

    @Test
    @DisplayName("Não se salta estados nem se volta atrás")
    void transicoesInvalidas() {
        assertFalse(EstadoEncomenda.PAGA.podeMudarPara(EstadoEncomenda.PRONTA));
        assertFalse(EstadoEncomenda.PRONTA.podeMudarPara(EstadoEncomenda.PAGA));
        assertFalse(EstadoEncomenda.ENTREGUE.podeMudarPara(EstadoEncomenda.PRONTA));
        assertFalse(EstadoEncomenda.CANCELADA.podeMudarPara(EstadoEncomenda.PAGA));
    }
}
