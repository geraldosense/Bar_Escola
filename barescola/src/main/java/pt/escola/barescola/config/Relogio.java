package pt.escola.barescola.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Fonte única da "hora atual" da aplicação.
 *
 * <p>Modo demonstração: com a variável de ambiente {@code BARESCOLA_MODO_DEMO=true}
 * (ou a propriedade de sistema {@code barescola.modoDemo=true}) a RN3 deixa de bloquear
 * encomendas — útil no Demo Day quando os intervalos do dia já passaram.</p>
 */
@ApplicationScoped
public class Relogio {

    public LocalDateTime agora() {
        return LocalDateTime.now();
    }

    public LocalDate hoje() {
        return LocalDate.now();
    }

    public LocalTime horaAtual() {
        return LocalTime.now();
    }

    public boolean modoDemo() {
        String valor = System.getProperty("barescola.modoDemo", System.getenv("BARESCOLA_MODO_DEMO"));
        return Boolean.parseBoolean(valor);
    }
}
