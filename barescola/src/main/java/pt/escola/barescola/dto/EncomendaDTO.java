package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import pt.escola.barescola.model.Encomenda;
import pt.escola.barescola.model.EstadoEncomenda;

public record EncomendaDTO(
        Long id,
        Integer alunoNumero,
        String alunoNome,
        String turma,
        Long intervaloId,
        String intervalo,
        LocalTime horaIntervalo,
        LocalDateTime dataHora,
        BigDecimal total,
        EstadoEncomenda estado,
        boolean podeCancelar,
        boolean entregaAutomatica,
        List<LinhaEncomendaDTO> linhas) implements Serializable {

    public static EncomendaDTO from(Encomenda e) {
        return new EncomendaDTO(
                e.getId(),
                e.getAluno().getNumero(),
                e.getAluno().getNome(),
                e.getAluno().getTurma(),
                e.getIntervalo().getId(),
                e.getIntervalo().getNome(),
                e.getIntervalo().getHoraInicio(),
                e.getDataHora(),
                e.getTotal(),
                e.getEstado(),
                e.getEstado().podeCancelar(),
                e.isEntregaAutomatica(),
                e.getLinhas().stream().map(LinhaEncomendaDTO::from).toList());
    }
}
