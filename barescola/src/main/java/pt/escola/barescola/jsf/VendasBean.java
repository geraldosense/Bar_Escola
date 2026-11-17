package pt.escola.barescola.jsf;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import pt.escola.barescola.dto.VendasDiaDTO;
import pt.escola.barescola.service.EncomendaService;
import pt.escola.barescola.service.VendasService;

/** Backoffice – vendas do dia (US07) e execução manual do fecho do dia (RN5). */
@Named
@ViewScoped
public class VendasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private VendasService vendasService;

    @EJB
    private EncomendaService encomendaService;

    private LocalDate data = LocalDate.now();
    private VendasDiaDTO vendas;

    @PostConstruct
    void init() {
        consultar();
    }

    public void consultar() {
        if (data == null) {
            data = LocalDate.now();
        }
        vendas = vendasService.vendasDoDia(data);
    }

    /** Demonstração da RN5 sem esperar pelas 23:30. */
    public void executarFechoDia() {
        int n = encomendaService.fecharDia(data);
        Mensagens.info(n + " encomenda(s) PRONTA passaram a ENTREGUE (registo no log do servidor).");
        consultar();
    }

    public LocalDate getData() { return data; }

    public void setData(LocalDate data) { this.data = data; }

    public VendasDiaDTO getVendas() { return vendas; }
}
