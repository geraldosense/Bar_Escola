package pt.escola.barescola.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Aluno com cartão de estudante. O {@code numero} é também o número do cartão.
 * O {@code email} é o nome de utilizador usado no login (liga a conta ao aluno).
 */
@Entity
@Table(name = "aluno")
@NamedQuery(name = Aluno.POR_EMAIL, query = "SELECT a FROM Aluno a WHERE a.email = :email")
@NamedQuery(name = Aluno.POR_NUMERO, query = "SELECT a FROM Aluno a WHERE a.numero = :numero")
@NamedQuery(name = Aluno.TODOS, query = "SELECT a FROM Aluno a ORDER BY a.turma, a.numero")
public class Aluno {

    public static final String POR_EMAIL = "Aluno.porEmail";
    public static final String POR_NUMERO = "Aluno.porNumero";
    public static final String TODOS = "Aluno.todos";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(nullable = false, unique = true)
    private Integer numero;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String turma;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @NotNull
    @PositiveOrZero(message = "O saldo não pode ser negativo")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @OneToMany(mappedBy = "aluno")
    private List<Encomenda> encomendas = new ArrayList<>();

    @Version
    private long versao;

    protected Aluno() {
        // JPA
    }

    public Aluno(int numero, String nome, String turma, String email, BigDecimal saldo) {
        this.numero = numero;
        this.nome = nome;
        this.turma = turma;
        this.email = email;
        this.saldo = saldo;
    }

    /** RN1 – o saldo cobre o valor? */
    public boolean temSaldoPara(BigDecimal valor) {
        return saldo.compareTo(valor) >= 0;
    }

    public void debitar(BigDecimal valor) {
        if (!temSaldoPara(valor)) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        saldo = saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        saldo = saldo.add(valor);
    }

    public Long getId() { return id; }

    public Integer getNumero() { return numero; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getTurma() { return turma; }

    public void setTurma(String turma) { this.turma = turma; }

    public String getEmail() { return email; }

    public BigDecimal getSaldo() { return saldo; }

    public List<Encomenda> getEncomendas() { return encomendas; }
}
