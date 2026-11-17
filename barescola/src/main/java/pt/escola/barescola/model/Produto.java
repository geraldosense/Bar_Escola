package pt.escola.barescola.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Produto vendido no bar. Relação N—1 com {@link Categoria}. */
@Entity
@Table(name = "produto")
@NamedQuery(name = Produto.TODOS,
        query = "SELECT p FROM Produto p JOIN FETCH p.categoria c ORDER BY c.nome, p.nome")
@NamedQuery(name = Produto.ATIVOS,
        query = "SELECT p FROM Produto p JOIN FETCH p.categoria c WHERE p.ativo = true ORDER BY c.nome, p.nome")
public class Produto {

    public static final String TODOS = "Produto.todos";
    public static final String ATIVOS = "Produto.ativos";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nome;

    @NotNull
    @DecimalMin(value = "0.01", message = "O preço tem de ser positivo")
    @Digits(integer = 4, fraction = 2)
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal preco;

    @NotNull
    @PositiveOrZero(message = "O stock não pode ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean ativo = true;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Controlo de concorrência otimista: dois pagamentos ao mesmo tempo não "vendem" o mesmo stock. */
    @Version
    private long versao;

    protected Produto() {
        // JPA
    }

    public Produto(String nome, BigDecimal preco, int stock, Categoria categoria) {
        this.nome = nome;
        this.preco = preco;
        this.stock = stock;
        this.categoria = categoria;
    }

    /** RN2 – há stock suficiente para a quantidade pedida? */
    public boolean temStock(int quantidade) {
        return stock != null && stock >= quantidade;
    }

    public boolean isDisponivel() {
        return ativo && stock != null && stock > 0;
    }

    public void baixarStock(int quantidade) {
        if (!temStock(quantidade)) {
            throw new IllegalStateException("Stock insuficiente de " + nome);
        }
        stock -= quantidade;
    }

    public void reporStock(int quantidade) {
        stock += quantidade;
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPreco() { return preco; }

    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getStock() { return stock; }

    public void setStock(Integer stock) { this.stock = stock; }

    public boolean isAtivo() { return ativo; }

    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Categoria getCategoria() { return categoria; }

    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
