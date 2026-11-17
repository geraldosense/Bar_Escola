package pt.escola.barescola.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/** Canal do ecrã do balcão: avisa que a fila mudou (nova encomenda, mudança de estado). Protegido no web.xml. */
@ServerEndpoint("/ws/balcao")
public class BalcaoEndpoint {

    @OnOpen
    public void abrir(Session sessao) {
        Notificador.registarBalcao(sessao);
    }

    @OnClose
    public void fechar(Session sessao) {
        Notificador.removerBalcao(sessao);
    }

    @OnError
    public void erro(Session sessao, Throwable t) {
        Notificador.removerBalcao(sessao);
    }
}
