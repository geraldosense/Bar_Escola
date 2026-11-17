package pt.escola.barescola.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CarrinhoTest {

    @Test
    void adicionarERemoverAtualizaOTotal() {
        Carrinho c = new Carrinho();
        c.adicionar(1L, "Água", new BigDecimal("0.60"));
        c.adicionar(1L, "Água", new BigDecimal("0.60"));
        c.adicionar(2L, "Maçã", new BigDecimal("0.40"));

        assertEquals(3, c.getNumeroArtigos());
        assertEquals(new BigDecimal("1.60"), c.getTotal());

        c.remover(1L);
        assertEquals(new BigDecimal("1.00"), c.getTotal());

        c.remover(1L);
        c.remover(2L);
        assertTrue(c.isVazio());
    }

    @Test
    void quantidadeTemLimite() {
        Carrinho c = new Carrinho();
        for (int i = 0; i < 30; i++) {
            c.adicionar(1L, "Café", new BigDecimal("0.55"));
        }
        assertEquals(Carrinho.MAX_POR_PRODUTO, c.getNumeroArtigos());
    }
}
