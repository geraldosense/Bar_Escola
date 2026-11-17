package pt.escola.barescola.jsf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import pt.escola.barescola.dto.AlunoDTO;
import pt.escola.barescola.service.AlunoService;
import pt.escola.barescola.service.exception.NegocioException;

/** Backoffice – carregamentos de saldo. */
@Named
@ViewScoped
public class SaldosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AlunoService servico;

    private List<AlunoDTO> alunos;
    private Integer numero;
    private BigDecimal valor;

    @PostConstruct
    void init() {
        alunos = servico.listar();
    }

    public void carregar() {
        try {
            AlunoDTO a = servico.carregarSaldo(numero, valor);
            Mensagens.info("Carregados " + valor + " € no cartão de " + a.nome() + ". Novo saldo: " + a.saldo() + " €");
            numero = null;
            valor = null;
            alunos = servico.listar();
        } catch (NegocioException e) {
            Mensagens.erro(e.getMessage());
        }
    }

    public List<AlunoDTO> getAlunos() { return alunos; }

    public Integer getNumero() { return numero; }

    public void setNumero(Integer numero) { this.numero = numero; }

    public BigDecimal getValor() { return valor; }

    public void setValor(BigDecimal valor) { this.valor = valor; }
}
