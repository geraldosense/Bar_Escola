package pt.escola.barescola.jms;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import pt.escola.barescola.service.EncomendaService;

/**
 * Etapa 8 – a "cozinha": consome a fila e põe cada encomenda EM_PREPARACAO, registando no log.
 * O pagamento responde logo ao aluno; o estado muda alguns segundos depois.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = CozinhaProducer.FILA_COZINHA),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
public class CozinhaMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(CozinhaMDB.class.getName());

    /** Simula o tempo até a cozinha pegar no pedido (apenas para a demonstração). */
    private static final long ATRASO_MS = 3_000;

    @EJB
    private EncomendaService encomendas;

    @Override
    public void onMessage(Message mensagem) {
        try {
            Long id = Long.valueOf(((TextMessage) mensagem).getText());
            LOG.info(() -> "Cozinha recebeu a encomenda #" + id);
            Thread.sleep(ATRASO_MS);
            encomendas.iniciarPreparacao(id);
        } catch (JMSException | NumberFormatException | ClassCastException e) {
            LOG.log(Level.SEVERE, "Mensagem inválida na fila da cozinha", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
