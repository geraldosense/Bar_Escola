package pt.escola.barescola.jsf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pt.escola.barescola.dto.CategoriaDTO;
import pt.escola.barescola.dto.ProdutoDTO;
import pt.escola.barescola.service.ProdutoService;
import pt.escola.barescola.service.exception.NegocioException;

/** Etapa 7 – backoffice JSF: produtos, preços e stock (US06). */
@Named
@ViewScoped
public class ProdutosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProdutoService servico;

    private List<ProdutoDTO> produtos;
    private List<CategoriaDTO> categorias;

    // formulário (novo/editar)
    private Long id;
    @NotBlank
    private String nome;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal preco;
    @NotNull
    @PositiveOrZero
    private Integer stock = 0;
    @NotNull
    private Long categoriaId;
    private boolean ativo = true;

    @PostConstruct
    void init() {
        categorias = servico.categorias();
        recarregar();
    }

    public void guardar() {
        try {
            ProdutoDTO dados = new ProdutoDTO(id, nome, preco, stock, ativo, categoriaId, null, false);
            if (id == null) {
                servico.criar(dados);
                Mensagens.info("Produto \"" + nome + "\" criado.");
            } else {
                servico.atualizar(id, dados);
                Mensagens.info("Produto \"" + nome + "\" atualizado.");
            }
            limpar();
            recarregar();
        } catch (NegocioException e) {
            Mensagens.erro(e.getMessage());
        }
    }

    public void editar(ProdutoDTO p) {
        id = p.id();
        nome = p.nome();
        preco = p.preco();
        stock = p.stock();
        categoriaId = p.categoriaId();
        ativo = p.ativo();
    }

    public void ajustarStock(ProdutoDTO p, int delta) {
        try {
            servico.atualizarStock(p.id(), Math.max(0, p.stock() + delta));
            recarregar();
        } catch (NegocioException e) {
            Mensagens.erro(e.getMessage());
        }
    }

    public void alternarAtivo(ProdutoDTO p) {
        servico.ativar(p.id(), !p.ativo());
        recarregar();
    }

    public void remover(ProdutoDTO p) {
        try {
            boolean apagado = servico.remover(p.id());
            Mensagens.info(apagado ? "Produto apagado." : "O produto já tinha vendas: foi desativado.");
            recarregar();
        } catch (NegocioException e) {
            Mensagens.erro(e.getMessage());
        }
    }

    public void limpar() {
        id = null;
        nome = null;
        preco = null;
        stock = 0;
        categoriaId = null;
        ativo = true;
    }

    private void recarregar() {
        produtos = servico.listar(false);
    }

    public boolean isEdicao() { return id != null; }

    public List<ProdutoDTO> getProdutos() { return produtos; }

    public List<CategoriaDTO> getCategorias() { return categorias; }

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPreco() { return preco; }

    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getStock() { return stock; }

    public void setStock(Integer stock) { this.stock = stock; }

    public Long getCategoriaId() { return categoriaId; }

    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public boolean isAtivo() { return ativo; }

    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
