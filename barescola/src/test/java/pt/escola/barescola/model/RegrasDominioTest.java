package pt.escola.barescola.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class RegrasDominioTest {

    private final Categoria bebidas = new Categoria("Bebidas");

    @Test
    void rn1SaldoTemDeCobrirOTotal() {
        Aluno aluno = new Aluno(1, "Ana", "10A", "ana@x.pt", new BigDecimal("2.00"));
        assertTrue(aluno.temSaldoPara(new BigDecimal("2.00")));
        assertFalse(aluno.temSaldoPara(new BigDecimal("2.01")));
        assertThrows(IllegalStateException.class, () -> aluno.debitar(new BigDecimal("5")));
    }

    @Test
    void rn2StockDesceEImpedeVendaSemStock() {
        Produto agua = new Produto("Água", new BigDecimal("0.60"), 2, bebidas);
        agua.baixarStock(2);
        assertEquals(0, agua.getStock());
        assertFalse(agua.isDisponivel());
        assertThrows(IllegalStateException.class, () -> agua.baixarStock(1));
    }

    @Test
    void rn3EncomendasFecham15MinutosAntes() {
        Intervalo intervalo = new Intervalo("Manhã", LocalTime.of(10, 0));
        assertEquals(LocalTime.of(9, 45), intervalo.getHoraFechoEncomendas());
        assertTrue(intervalo.aceitaEncomendas(LocalTime.of(9, 44)));
        assertFalse(intervalo.aceitaEncomendas(LocalTime.of(9, 45)));
    }

    @Test
    void totalDaEncomendaSomaAsLinhasComPrecoCongelado() {
        Produto sumo = new Produto("Sumo", new BigDecimal("1.20"), 10, bebidas);
        Aluno aluno = new Aluno(1, "Ana", "10A", "ana@x.pt", BigDecimal.TEN);
        Encomenda e = new Encomenda(aluno, new Intervalo("Manhã", LocalTime.NOON), LocalDateTime.now());

        e.adicionarLinha(new LinhaEncomenda(sumo, 3));
        sumo.setPreco(new BigDecimal("9.99")); // mudar o preço depois não altera a encomenda

        assertEquals(new BigDecimal("3.60"), e.getTotal());
    }

    @Test
    void rn5EntregaAutomaticaSoAPartirDePronta() {
        Aluno aluno = new Aluno(1, "Ana", "10A", "ana@x.pt", BigDecimal.TEN);
        Encomenda e = new Encomenda(aluno, new Intervalo("Manhã", LocalTime.NOON), LocalDateTime.now());
        assertThrows(IllegalStateException.class, e::marcarEntregaAutomatica);

        e.mudarEstado(EstadoEncomenda.EM_PREPARACAO);
        e.mudarEstado(EstadoEncomenda.PRONTA);
        e.marcarEntregaAutomatica();
        assertEquals(EstadoEncomenda.ENTREGUE, e.getEstado());
        assertTrue(e.isEntregaAutomatica());
    }
}
