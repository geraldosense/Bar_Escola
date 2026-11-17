package pt.escola.barescola.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** Atalhos para mensagens JSF. */
final class Mensagens {

    private Mensagens() {
    }

    static void info(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, texto, null));
    }

    static void erro(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }
}
