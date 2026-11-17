package pt.escola.barescola.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Categoria de produtos (bebidas, sandes, fruta...). Relação 1—N com {@link Produto}. */
@Entity
@Table(name = "categoria")
@NamedQuery(name = Categoria.TODAS, query = "SELECT c FROM Categoria c ORDER BY c.nome")
public class Categoria {

    public static final String TODAS = "Categoria.todas";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @OneToMany(mappedBy = "categoria")
    @OrderBy("nome")
    private List<Produto> produtos = new ArrayList<>();

    protected Categoria() {
        // JPA
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public List<Produto> getProdutos() { return produtos; }
}
