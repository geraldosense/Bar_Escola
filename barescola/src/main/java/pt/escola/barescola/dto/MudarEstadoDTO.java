package pt.escola.barescola.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import pt.escola.barescola.model.EstadoEncomenda;

/** Corpo do PUT /api/encomendas/{id}/estado. */
public record MudarEstadoDTO(@NotNull EstadoEncomenda estado) implements Serializable {
}
