package pt.escola.barescola.dto;

import java.io.Serializable;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Corpo do POST /api/encomendas. */
public record CriarEncomendaDTO(@NotNull Long intervaloId, @NotEmpty List<@Valid LinhaPedidoDTO> linhas) implements Serializable {
}
