package pt.escola.barescola.web;

import java.io.IOException;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pt.escola.barescola.service.IntervaloService;
import pt.escola.barescola.service.ProdutoService;

/** Etapa 3 – menu do dia gerado no servidor (Servlet + JSP). Versão "clássica" do ecrã do aluno. */
@WebServlet(name = "MenuServlet", urlPatterns = "/menu")
public class MenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProdutoService produtos;

    @EJB
    private IntervaloService intervalos;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sessao = req.getSession();
        req.setAttribute("produtos", produtos.listar(true));
        req.setAttribute("intervalos", intervalos.listar());
        req.setAttribute("carrinho", Carrinho.daSessao(sessao));

        // mensagens "flash" (Post/Redirect/Get)
        moverFlash(sessao, req, "sucesso");
        moverFlash(sessao, req, "erro");

        req.getRequestDispatcher("/WEB-INF/jsp/menu.jsp").forward(req, resp);
    }

    private static void moverFlash(HttpSession sessao, HttpServletRequest req, String chave) {
        Object valor = sessao.getAttribute("flash." + chave);
        if (valor != null) {
            req.setAttribute(chave, valor);
            sessao.removeAttribute("flash." + chave);
        }
    }
}
