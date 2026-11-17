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
            case PAGA -> "Encomenda #" + ev.encomendaId() + " paga. Vai para a cozinha.";
            case EM_PREPARACAO -> "A tua encomenda #" + ev.encomendaId() + " está a ser preparada.";
            case PRONTA -> "O teu pedido #" + ev.encomendaId() + " está pronto! Levanta-o no balcão.";
            case ENTREGUE -> "Encomenda #" + ev.encomendaId() + " entregue. Bom lanche!";
            case CANCELADA -> "Encomenda #" + ev.encomendaId() + " cancelada. O saldo foi reposto.";
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
