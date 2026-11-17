package pt.escola.barescola.dto;

import java.io.Serializable;

import java.util.List;

/** Formato único de erro devolvido pela API. */
public record ErroDTO(int status, String codigo, String mensagem, List<String> detalhes) implements Serializable {

    public ErroDTO(int status, String codigo, String mensagem) {
        this(status, codigo, mensagem, List.of());
    }
}
