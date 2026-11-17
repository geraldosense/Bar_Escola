package pt.escola.barescola.dto;

import java.io.Serializable;

import java.time.LocalDateTime;

/** Resposta do GET /api/estado (etapa 1). */
public record EstadoAppDTO(String aplicacao, String versao, String estado, LocalDateTime hora) implements Serializable {
}
