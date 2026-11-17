package pt.escola.barescola.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.Session;

/**
 * Registo das ligações WebSocket abertas.
 * <ul>
 *   <li>alunos: agrupadas pelo utilizador (email) → avisos "o teu pedido está pronto"</li>
 *   <li>balcão: ecrãs dos funcionários → "a fila mudou, atualiza"</li>
 * </ul>
 */
public final class Notificador {

    private static final Logger LOG = Logger.getLogger(Notificador.class.getName());

    private static final Map<String, Set<Session>> ALUNOS = new ConcurrentHashMap<>();
    private static final Set<Session> BALCAO = ConcurrentHashMap.newKeySet();

    private Notificador() {
    }

    static void registarAluno(String email, Session sessao) {
        ALUNOS.computeIfAbsent(email.toLowerCase(), k -> ConcurrentHashMap.newKeySet()).add(sessao);
    }

    static void removerAluno(String email, Session sessao) {
        Set<Session> sessoes = ALUNOS.get(email.toLowerCase());
        if (sessoes != null) {
            sessoes.remove(sessao);
        }
    }

    static void registarBalcao(Session sessao) {
        BALCAO.add(sessao);
    }

    static void removerBalcao(Session sessao) {
        BALCAO.remove(sessao);
    }

    public static void enviarAluno(String email, String json) {
        Set<Session> sessoes = ALUNOS.get(email.toLowerCase());
        if (sessoes != null) {
            sessoes.forEach(s -> enviar(s, json));
        }
    }

    public static void enviarBalcao(String json) {
        BALCAO.forEach(s -> enviar(s, json));
    }

    private static void enviar(Session sessao, String json) {
        if (!sessao.isOpen()) {
            return;
        }
        try {
            synchronized (sessao) { // a API não permite envios concorrentes na mesma sessão
                sessao.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "Falha a enviar aviso WebSocket", e);
        }
    }
}
