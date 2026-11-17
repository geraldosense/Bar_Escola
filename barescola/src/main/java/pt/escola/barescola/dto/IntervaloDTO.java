package pt.escola.barescola.dto;

import java.io.Serializable;

import java.time.LocalTime;

import pt.escola.barescola.model.Intervalo;

/** Intervalo com a indicação se ainda aceita encomendas agora (RN3). */
public record IntervaloDTO(Long id, String nome, LocalTime horaInicio, LocalTime horaFechoEncomendas, boolean aberto) implements Serializable {

    public static IntervaloDTO from(Intervalo i, boolean aberto) {
        return new IntervaloDTO(i.getId(), i.getNome(), i.getHoraInicio(), i.getHoraFechoEncomendas(), aberto);
    }
}
