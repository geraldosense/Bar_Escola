package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

import pt.escola.barescola.model.Aluno;

public record AlunoDTO(Integer numero, String nome, String turma, String email, BigDecimal saldo) implements Serializable {

    public static AlunoDTO from(Aluno a) {
        return new AlunoDTO(a.getNumero(), a.getNome(), a.getTurma(), a.getEmail(), a.getSaldo());
    }
}
