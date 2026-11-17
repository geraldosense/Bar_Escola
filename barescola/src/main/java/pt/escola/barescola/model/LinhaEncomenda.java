package pt.escola.barescola.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Um produto e a quantidade dentro de uma encomenda.
 * O preço unitário é copiado do produto no momento da compra (o preço pode mudar depois).
 */
@Entity
@Table(name = "linha_encomenda")
public class LinhaEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Min(1)
    @Max(20)
    @Column(nullable = false)
    private int quantidade;

    @NotNull
    @Column(name = "preco_unitario", nullable = false, precision = 6, scale = 2)
    private BigDecimal precoUnitario;

    protected LinhaEncomenda() {
        // JPA
    }

    public LinhaEncomenda(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    void setEncomenda(Encomenda encomenda) {
        this.encomenda = encomenda;
    }

    public Long getId() { return id; }

    public Encomenda getEncomenda() { return encomenda; }

    public Produto getProduto() { return produto; }

    public int getQuantidade() { return quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
