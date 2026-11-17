package pt.escola.barescola.web;

import java.io.IOException;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pt.escola.barescola.dto.CriarEncomendaDTO;
import pt.escola.barescola.dto.PagamentoDTO;
import pt.escola.barescola.service.EncomendaService;
import pt.escola.barescola.service.exception.NegocioException;

/** Etapa 3 – cria (e paga) a encomenda a partir do carrinho da sessão. */
@WebServlet(name = "EncomendaServlet", urlPatterns = "/encomendar")
public class EncomendaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private EncomendaService encomendas;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sessao = req.getSession();
        Carrinho carrinho = Carrinho.daSessao(sessao);

        try {
            Long intervaloId = Long.valueOf(req.getParameter("intervaloId"));
            PagamentoDTO pagamento = encomendas.pagar(new CriarEncomendaDTO(intervaloId, carrinho.paraLinhas()));
            carrinho.limpar();
            sessao.setAttribute("flash.sucesso", "Encomenda #" + pagamento.encomenda().id()
                    + " paga! Saldo restante: " + pagamento.saldoRestante() + " €");
        } catch (NumberFormatException e) {
            sessao.setAttribute("flash.erro", "Escolha um intervalo.");
        } catch (NegocioException e) {
            sessao.setAttribute("flash.erro", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/menu");
    }
}
