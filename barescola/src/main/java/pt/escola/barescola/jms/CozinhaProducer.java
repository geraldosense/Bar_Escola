package pt.escola.barescola.jms;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.Queue;

/**
 * Etapa 8 – coloca as encomendas pagas na fila da cozinha.
 *
 * <p>A fila é criada pelo próprio deploy ({@code @JMSDestinationDefinition}) – não é preciso
 * configurá-la à mão no WildFly (mas é preciso arrancar com {@code standalone-full.xml}).</p>
 *
 * <p>O envio participa na transação JTA de quem chama: se o pagamento fizer rollback,
 * a mensagem nunca chega à cozinha.</p>
 */
@JMSDestinationDefinition(
        name = CozinhaProducer.FILA_COZINHA,
        interfaceName = "jakarta.jms.Queue",
        destinationName = "CozinhaQueue")
@Stateless
@PermitAll
public class CozinhaProducer {

    public static final String FILA_COZINHA = "java:global/jms/CozinhaQueue";

    @Inject
    private JMSContext jms;

    @Resource(lookup = FILA_COZINHA)
    private Queue fila;

    public void enviar(Long encomendaId) {
        jms.createProducer()
                .setProperty("tipo", "NOVA_ENCOMENDA")
                .send(fila, String.valueOf(encomendaId));
    }
}
