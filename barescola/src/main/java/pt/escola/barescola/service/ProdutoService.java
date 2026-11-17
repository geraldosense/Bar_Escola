package pt.escola.barescola.service;

import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.CategoriaDTO;
import pt.escola.barescola.dto.ProdutoDTO;
import pt.escola.barescola.model.Categoria;
import pt.escola.barescola.model.Produto;
import pt.escola.barescola.service.exception.DadosInvalidosException;
import pt.escola.barescola.service.exception.NaoEncontradoException;

/** US01 (menu) e US06 (gestão de produtos, preços e stock). */
@Stateless
@RolesAllowed(Papeis.GESTOR)
public class ProdutoService {

    @PersistenceContext
    private EntityManager em;

    /** Menu do dia (só ativos) ou lista completa para o backoffice. */
    @PermitAll
    public List<ProdutoDTO> listar(boolean apenasAtivos) {
        String query = apenasAtivos ? Produto.ATIVOS : Produto.TODOS;
        return em.createNamedQuery(query, Produto.class).getResultList().stream()
                .map(ProdutoDTO::from)
                .toList();
    }

    @PermitAll
    public ProdutoDTO obter(Long id) {
        return ProdutoDTO.from(encontrar(id));
    }

    @PermitAll
    public List<CategoriaDTO> categorias() {
        return em.createNamedQuery(Categoria.TODAS, Categoria.class).getResultList().stream()
                .map(CategoriaDTO::from)
                .toList();
    }

    public ProdutoDTO criar(ProdutoDTO dados) {
        Produto produto = new Produto(dados.nome().trim(), dados.preco(), dados.stock(), categoria(dados.categoriaId()));
        produto.setAtivo(dados.ativo());
        em.persist(produto);
        return ProdutoDTO.from(produto);
    }

    public ProdutoDTO atualizar(Long id, ProdutoDTO dados) {
        Produto produto = encontrar(id);
        produto.setNome(dados.nome().trim());
        produto.setPreco(dados.preco());
        produto.setStock(dados.stock());
        produto.setAtivo(dados.ativo());
        produto.setCategoria(categoria(dados.categoriaId()));
        return ProdutoDTO.from(produto);
    }

    public ProdutoDTO atualizarStock(Long id, int stock) {
        if (stock < 0) {
            throw new DadosInvalidosException("O stock não pode ser negativo.");
        }
        Produto produto = encontrar(id);
        produto.setStock(stock);
        return ProdutoDTO.from(produto);
    }

    public ProdutoDTO ativar(Long id, boolean ativo) {
        Produto produto = encontrar(id);
        produto.setAtivo(ativo);
        return ProdutoDTO.from(produto);
    }

    /**
     * Remove o produto. Se já tiver sido vendido, as encomendas antigas precisam dele,
     * por isso é apenas desativado (deixa de aparecer no menu).
     *
     * @return {@code true} se foi apagado; {@code false} se foi só desativado
     */
    public boolean remover(Long id) {
        Produto produto = encontrar(id);
        Long vendas = em.createQuery("SELECT COUNT(l) FROM LinhaEncomenda l WHERE l.produto = :p", Long.class)
                .setParameter("p", produto)
                .getSingleResult();
        if (vendas > 0) {
            produto.setAtivo(false);
            return false;
        }
        em.remove(produto);
        return true;
    }

    private Produto encontrar(Long id) {
        Produto produto = em.find(Produto.class, id);
        if (produto == null) {
            throw new NaoEncontradoException("Produto", id);
        }
        return produto;
    }

    private Categoria categoria(Long id) {
        Categoria categoria = em.find(Categoria.class, id);
        if (categoria == null) {
            throw new NaoEncontradoException("Categoria", id);
        }
        return categoria;
    }
}
