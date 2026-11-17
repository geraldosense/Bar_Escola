package pt.escola.barescola.config;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.model.Aluno;
import pt.escola.barescola.model.Categoria;
import pt.escola.barescola.model.Intervalo;
import pt.escola.barescola.model.Produto;

/**
 * Carrega dados de exemplo na primeira execução (BD vazia).
 * Os alunos aqui criados correspondem aos utilizadores criados por scripts/setup-wildfly.*
 */
@Singleton
@Startup
public class DadosIniciais {

    private static final Logger LOG = Logger.getLogger(DadosIniciais.class.getName());

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    void carregar() {
        Long categorias = em.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class).getSingleResult();
        if (categorias > 0) {
            return;
        }
        LOG.info("BD vazia: a carregar dados de exemplo do BarEscola...");

        Categoria bebidas = categoria("Bebidas");
        Categoria sandes = categoria("Sandes");
        Categoria fruta = categoria("Fruta");
        Categoria bolos = categoria("Bolos e salgados");

        produto("Água 50 cl", "0.60", 40, bebidas);
        produto("Sumo de laranja natural", "1.20", 15, bebidas);
        produto("Leite com chocolate", "0.80", 25, bebidas);
        produto("Café", "0.55", 100, bebidas);
        produto("Sandes de fiambre", "1.30", 20, sandes);
        produto("Sandes mista", "1.60", 18, sandes);
        produto("Tosta mista", "1.90", 12, sandes);
        produto("Sandes de frango", "2.10", 1, sandes);     // stock 1 → bom para demonstrar a RN2
        produto("Maçã", "0.40", 30, fruta);
        produto("Banana", "0.40", 0, fruta);                // esgotado → aparece "indisponível"
        produto("Croissant simples", "0.90", 15, bolos);
        produto("Pão de queijo", "0.70", 25, bolos);
        produto("Rissol de carne", "1.10", 10, bolos);

        em.persist(new Aluno(1001, "Ana Silva", "10A", "aluno1@barescola.pt", new BigDecimal("15.00")));
        em.persist(new Aluno(1002, "Bruno Costa", "11B", "aluno2@barescola.pt", new BigDecimal("2.00")));
        em.persist(new Aluno(1003, "Carla Mendes", "12C", "aluno3@barescola.pt", new BigDecimal("8.50")));

        em.persist(new Intervalo("Intervalo da manhã", LocalTime.of(10, 5)));
        em.persist(new Intervalo("2.º intervalo", LocalTime.of(11, 50)));
        em.persist(new Intervalo("Almoço", LocalTime.of(13, 20)));
        em.persist(new Intervalo("Intervalo da tarde", LocalTime.of(15, 5)));
        em.persist(new Intervalo("Último intervalo", LocalTime.of(16, 50)));

        LOG.info("Dados de exemplo carregados.");
    }

    private Categoria categoria(String nome) {
        Categoria c = new Categoria(nome);
        em.persist(c);
        return c;
    }

    private void produto(String nome, String preco, int stock, Categoria categoria) {
        em.persist(new Produto(nome, new BigDecimal(preco), stock, categoria));
    }
}
