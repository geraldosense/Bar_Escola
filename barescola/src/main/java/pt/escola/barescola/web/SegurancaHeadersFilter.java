package pt.escola.barescola.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Etapa 10 – cabeçalhos HTTP de segurança em todas as respostas. */
@WebFilter(filterName = "SegurancaHeadersFilter", urlPatterns = "/*")
public class SegurancaHeadersFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        if (req.getRequestURI().contains("/api/")) {
            resp.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(req, resp);
    }
}
