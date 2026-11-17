package pt.escola.barescola.dto;

import java.io.Serializable;

/** Quem está autenticado (GET /api/me). {@code aluno} só vem preenchido para o papel ALUNO. */
public record UtilizadorDTO(String username, String papel, AlunoDTO aluno) implements Serializable {
}
