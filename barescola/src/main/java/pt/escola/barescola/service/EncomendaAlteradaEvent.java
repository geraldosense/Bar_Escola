package pt.escola.barescola.service;

import pt.escola.barescola.model.EstadoEncomenda;

/**
 * Evento CDI disparado sempre que uma encomenda é criada ou muda de estado.
 * É observado depois do commit (AFTER_SUCCESS) para enviar avisos por WebSocket.
 */
public record EncomendaAlteradaEvent(Long encomendaId, String emailAluno, EstadoEncomenda estado) {
}
