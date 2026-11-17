package pt.escola.barescola.dto;

import java.io.Serializable;

import pt.escola.barescola.model.Categoria;

public record CategoriaDTO(Long id, String nome) implements Serializable {

    public static CategoriaDTO from(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNome());
    }
}
