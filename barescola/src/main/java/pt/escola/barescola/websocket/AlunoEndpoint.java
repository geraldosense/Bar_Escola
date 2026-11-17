package pt.escola.barescola.websocket;

import java.security.Principal;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Etapa 7 – canal do aluno: recebe "o teu pedido está pronto" sem recarregar a página.
 * O handshake está protegido no web.xml (papel ALUNO), por isso há sempre um utilizador autenticado.
 */
@ServerEndpoint("/ws/aluno")
public class AlunoEndpoint {

    @OnOpen
    public void abrir(Session sessao) throws java.io.IOException {
        Principal p = sessao.getUserPrincipal();
        if (p == null) {
            sessao.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Não autenticado"));
            return;
        }
        Notificador.registarAluno(p.getName(), sessao);
    }

    @OnClose
    public void fechar(Session sessao) {
        Principal p = sessao.getUserPrincipal();
        if (p != null) {
            Notificador.removerAluno(p.getName(), sessao);
        }
    }

    @OnError
    public void erro(Session sessao, Throwable t) {
        fechar(sessao);
    }
}
