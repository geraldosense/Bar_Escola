package pt.escola.barescola.web;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pt.escola.barescola.config.Papeis;

/** Depois do login, envia cada utilizador para o seu ecrã inicial conforme o papel. */
@WebServlet(name = "InicioServlet", urlPatterns = "/inicio")
public class InicioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        if (req.isUserInRole(Papeis.GESTOR)) {
            resp.sendRedirect(ctx + "/backoffice/produtos.xhtml");
        } else if (req.isUserInRole(Papeis.FUNCIONARIO)) {
            resp.sendRedirect(ctx + "/balcao/fila.html");
        } else if (req.isUserInRole(Papeis.ALUNO)) {
            resp.sendRedirect(ctx + "/aluno/menu.html");
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
