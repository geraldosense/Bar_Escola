package pt.escola.barescola.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.json.Json;
import pt.escola.barescola.model.EstadoEncomenda;
import pt.escola.barescola.service.EncomendaAlteradaEvent;

/**
 * Transforma eventos de negócio em avisos WebSocket.
 * {@code AFTER_SUCCESS}: só avisa depois do commit – nunca se avisa de algo que fez rollback.
 */
@ApplicationScoped
public class NotificacaoObserver {

    public void encomendaAlterada(@Observes(during = TransactionPhase.AFTER_SUCCESS) EncomendaAlteradaEvent ev) {
        String tipo = ev.estado() == EstadoEncomenda.PRONTA ? "PEDIDO_PRONTO" : "ESTADO_ALTERADO";
        String mensagem = switch (ev.estado()) {
            case PAGA -> "Senha " + ev.encomendaId() + " paga. Já seguiu para a cozinha.";
            case EM_PREPARACAO -> "A senha " + ev.encomendaId() + " está a ser preparada.";
            case PRONTA -> "A senha " + ev.encomendaId() + " está pronta. Podes levantar no balcão.";
            case ENTREGUE -> "Senha " + ev.encomendaId() + " entregue.";
            case CANCELADA -> "Senha " + ev.encomendaId() + " cancelada. O valor voltou para o cartão.";
        };

        String json = Json.createObjectBuilder()
                .add("tipo", tipo)
                .add("encomendaId", ev.encomendaId())
                .add("estado", ev.estado().name())
                .add("mensagem", mensagem)
                .build()
                .toString();

        Notificador.enviarAluno(ev.emailAluno(), json);
        Notificador.enviarBalcao(json);
    }
}
