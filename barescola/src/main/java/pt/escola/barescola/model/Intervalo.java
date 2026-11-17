package pt.escola.barescola.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pausa entre aulas em que se levantam as encomendas. Relação 1—N com {@link Encomenda}. */
@Entity
@Table(name = "intervalo")
@NamedQuery(name = Intervalo.TODOS, query = "SELECT i FROM Intervalo i ORDER BY i.horaInicio")
public class Intervalo {

    public static final String TODOS = "Intervalo.todos";

    /** RN3 – as encomendas fecham 15 minutos antes do início do intervalo. */
    public static final int MINUTOS_FECHO_ANTECIPADO = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String nome;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(name = "hora_fecho_encomendas", nullable = false)
    private LocalTime horaFechoEncomendas;

    protected Intervalo() {
        // JPA
    }

    public Intervalo(String nome, LocalTime horaInicio) {
        this.nome = nome;
        this.horaInicio = horaInicio;
        this.horaFechoEncomendas = horaInicio.minusMinutes(MINUTOS_FECHO_ANTECIPADO);
    }

    /** RN3 – ainda se aceitam encomendas a esta hora? */
    public boolean aceitaEncomendas(LocalTime agora) {
        return agora.isBefore(horaFechoEncomendas);
    }

    @AssertTrue(message = "As encomendas têm de fechar antes do início do intervalo")
    public boolean isFechoAntesDoInicio() {
        return horaInicio == null || horaFechoEncomendas == null || horaFechoEncomendas.isBefore(horaInicio);
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public LocalTime getHoraInicio() { return horaInicio; }

    public LocalTime getHoraFechoEncomendas() { return horaFechoEncomendas; }
}
