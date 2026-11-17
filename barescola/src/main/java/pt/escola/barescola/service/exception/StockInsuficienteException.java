package pt.escola.barescola.service.exception;

/** RN2 – não há stock de um dos produtos. */
public class StockInsuficienteException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public StockInsuficienteException(String produto, int disponivel) {
        super("STOCK_INSUFICIENTE", 409,
                "Stock insuficiente de \"" + produto + "\" (disponível: " + disponivel + ").");
    }
}
