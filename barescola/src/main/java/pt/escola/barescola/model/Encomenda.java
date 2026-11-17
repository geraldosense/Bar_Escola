package pt.escola.barescola.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Pedido de um aluno para um intervalo, com uma ou mais linhas. */
@Entity
@Table(name = "encomenda")
@NamedQuery(name = Encomenda.DO_ALUNO,
        query = "SELECT e FROM Encomenda e WHERE e.aluno.email = :email ORDER BY e.dataHora DESC")
@NamedQuery(name = Encomenda.FILA,
        query = """
                SELECT e FROM Encomenda e
                WHERE e.dataHora >= :inicio AND e.dataHora < :fim
                  AND e.estado IN :estados
                ORDER BY e.intervalo.horaInicio, e.dataHora
                """)
@NamedQuery(name = Encomenda.PRONTAS_ATE,
        query = "SELECT e FROM Encomenda e WHERE e.estado = :estado AND e.dataHora < :limite")
public class Encomenda {

    public static final String DO_ALUNO = "Encomenda.doAluno";
    public static final String FILA = "Encomenda.fila";
    public static final String PRONTAS_ATE = "Encomenda.prontasAte";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "intervalo_id", nullable = false)
    private Intervalo intervalo;

    @NotNull
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEncomenda estado = EstadoEncomenda.PAGA;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    /** RN5 – marcada como ENTREGUE automaticamente pelo timer de fecho do dia. */
    @Column(name = "entrega_automatica", nullable = false)
    private boolean entregaAutomatica;

    @NotEmpty
    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LinhaEncomenda> linhas = new ArrayList<>();

    @Version
    private long versao;

    protected Encomenda() {
        // JPA
    }

    public Encomenda(Aluno aluno, Intervalo intervalo, LocalDateTime dataHora) {
        this.aluno = aluno;
        this.intervalo = intervalo;
        this.dataHora = dataHora;
        this.dataAtualizacao = dataHora;
    }

    public void adicionarLinha(LinhaEncomenda linha) {
        linha.setEncomenda(this);
        linhas.add(linha);
        total = total.add(linha.getSubtotal());
    }

    /** Muda o estado validando a transição (ver {@link EstadoEncomenda#podeMudarPara}). */
    public void mudarEstado(EstadoEncomenda novo) {
        if (!estado.podeMudarPara(novo)) {
            throw new IllegalStateException("Transição inválida: " + estado + " → " + novo);
        }
        this.estado = novo;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void marcarEntregaAutomatica() {
        mudarEstado(EstadoEncomenda.ENTREGUE);
        this.entregaAutomatica = true;
    }

    public Long getId() { return id; }

    public Aluno getAluno() { return aluno; }

    public Intervalo getIntervalo() { return intervalo; }

    public LocalDateTime getDataHora() { return dataHora; }

    public BigDecimal getTotal() { return total; }

    public EstadoEncomenda getEstado() { return estado; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public boolean isEntregaAutomatica() { return entregaAutomatica; }

    public List<LinhaEncomenda> getLinhas() { return linhas; }
}
