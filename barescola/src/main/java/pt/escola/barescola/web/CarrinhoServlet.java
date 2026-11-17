package pt.escola.barescola.web;

import java.io.IOException;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pt.escola.barescola.dto.ProdutoDTO;
import pt.escola.barescola.service.ProdutoService;
import pt.escola.barescola.service.exception.NegocioException;

/** Etapa 3 – adicionar/remover produtos do carrinho da sessão. */
@WebServlet(name = "CarrinhoServlet", urlPatterns = "/carrinho")
public class CarrinhoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProdutoService produtos;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Carrinho carrinho = Carrinho.daSessao(req.getSession());
        String acao = req.getParameter("acao");

        try {
            switch (acao == null ? "" : acao) {
                case "adicionar" -> {
                    ProdutoDTO p = produtos.obter(Long.valueOf(req.getParameter("produtoId")));
                    if (p.disponivel()) {
                        carrinho.adicionar(p.id(), p.nome(), p.preco());
                    } else {
                        req.getSession().setAttribute("flash.erro", p.nome() + " está indisponível.");
                    }
                }
                case "remover" -> carrinho.remover(Long.valueOf(req.getParameter("produtoId")));
                case "limpar" -> carrinho.limpar();
                default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação desconhecida");
            }
        } catch (NumberFormatException | NegocioException e) {
            req.getSession().setAttribute("flash.erro", "Produto inválido.");
        }
        if (!resp.isCommitted()) {
            resp.sendRedirect(req.getContextPath() + "/menu");
        }
    }
}
