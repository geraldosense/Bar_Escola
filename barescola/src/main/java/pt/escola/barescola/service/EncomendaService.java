package pt.escola.barescola.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.config.Relogio;
import pt.escola.barescola.dto.CriarEncomendaDTO;
import pt.escola.barescola.dto.EncomendaDTO;
import pt.escola.barescola.dto.LinhaPedidoDTO;
import pt.escola.barescola.dto.PagamentoDTO;
import pt.escola.barescola.jms.CozinhaProducer;
import pt.escola.barescola.model.Aluno;
import pt.escola.barescola.model.Encomenda;
import pt.escola.barescola.model.EstadoEncomenda;
import pt.escola.barescola.model.Intervalo;
import pt.escola.barescola.model.LinhaEncomenda;
import pt.escola.barescola.model.Produto;
import pt.escola.barescola.service.exception.AcessoNegadoException;
import pt.escola.barescola.service.exception.DadosInvalidosException;
import pt.escola.barescola.service.exception.EncomendasFechadasException;
import pt.escola.barescola.service.exception.EstadoInvalidoException;
import pt.escola.barescola.service.exception.NaoEncontradoException;
import pt.escola.barescola.service.exception.SaldoInsuficienteException;
import pt.escola.barescola.service.exception.StockInsuficienteException;

/**
 * Coração do negócio: criar/pagar, cancelar e mudar o estado das encomendas.
 *
 * <p>Cada método público corre numa transação JTA gerida pelo contentor (REQUIRED por omissão).
 * Se for lançada uma {@code NegocioException} (anotada com {@code rollback = true})
 * <strong>nada</strong> fica gravado: nem o saldo descontado, nem o stock baixado, nem a encomenda.</p>
 *
 * <ul>
 *   <li>RN1 – só se encomenda se o saldo cobrir o total → {@link #pagar}</li>
 *   <li>RN2 – só se encomenda se houver stock; o stock desce ao pagar → {@link #pagar}</li>
 *   <li>RN3 – encomendas fecham 15 min antes do intervalo → {@link #pagar}</li>
 *   <li>RN4 – só se cancela em PAGA; saldo e stock repostos → {@link #cancelar}</li>
 *   <li>RN5 – no fim do dia, PRONTA passa a ENTREGUE → {@link #fecharDia} (chamado pelo timer)</li>
 * </ul>
 */
@Stateless
@DeclareRoles({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
public class EncomendaService {

    private static final Logger LOG = Logger.getLogger(EncomendaService.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext ctx;

    @Inject
    private Relogio relogio;

    @Inject
    private Event<EncomendaAlteradaEvent> eventos;

    @EJB
    private CozinhaProducer cozinha;

    // ------------------------------------------------------------------ ALUNO

    /**
     * Cria e paga uma encomenda numa única transação (US02 + US03).
     *
     * <p>Ordem propositada: primeiro desconta o saldo e só depois baixa o stock produto a produto.
     * Se o stock falhar a meio, a exceção provoca rollback e o saldo volta ao valor original —
     * é a demonstração pedida no enunciado.</p>
     */
    @RolesAllowed(Papeis.ALUNO)
    public PagamentoDTO pagar(CriarEncomendaDTO pedido) {
        if (pedido == null || pedido.linhas() == null || pedido.linhas().isEmpty()) {
            throw new DadosInvalidosException("O carrinho está vazio.");
        }

        Aluno aluno = alunoAutenticado(LockModeType.PESSIMISTIC_WRITE);
        Intervalo intervalo = em.find(Intervalo.class, pedido.intervaloId());
        if (intervalo == null) {
            throw new NaoEncontradoException("Intervalo", pedido.intervaloId());
        }

        // RN3 – prazo de encomenda
        if (!relogio.modoDemo() && !intervalo.aceitaEncomendas(relogio.horaAtual())) {
            throw new EncomendasFechadasException(intervalo.getNome(), intervalo.getHoraFechoEncomendas());
        }

        // Montar a encomenda (o preço unitário fica "congelado" em cada linha)
        Encomenda encomenda = new Encomenda(aluno, intervalo, relogio.agora());
        for (LinhaPedidoDTO linha : pedido.linhas()) {
            Produto produto = em.find(Produto.class, linha.produtoId(), LockModeType.PESSIMISTIC_WRITE);
            if (produto == null) {
                throw new NaoEncontradoException("Produto", linha.produtoId());
            }
            if (!produto.isAtivo()) {
                throw new DadosInvalidosException("O produto \"" + produto.getNome() + "\" já não está à venda.");
            }
            if (linha.quantidade() < 1) {
                throw new DadosInvalidosException("Quantidade inválida para \"" + produto.getNome() + "\".");
            }
            encomenda.adicionarLinha(new LinhaEncomenda(produto, linha.quantidade()));
        }

        // RN1 – saldo
        BigDecimal total = encomenda.getTotal();
        if (!aluno.temSaldoPara(total)) {
            throw new SaldoInsuficienteException(aluno.getSaldo(), total);
        }
        aluno.debitar(total);

        // RN2 – stock (desce ao pagar). Uma falha aqui desfaz também o débito acima.
        for (LinhaEncomenda linha : encomenda.getLinhas()) {
            Produto produto = linha.getProduto();
            if (!produto.temStock(linha.getQuantidade())) {
                throw new StockInsuficienteException(produto.getNome(), produto.getStock());
            }
            produto.baixarStock(linha.getQuantidade());
        }

        em.persist(encomenda);
        em.flush(); // obtém o id antes de enviar para a fila

        // Etapa 8 – envia para a fila da cozinha (JMS). A mensagem só é entregue se a transação fizer commit.
        cozinha.enviar(encomenda.getId());
        eventos.fire(new EncomendaAlteradaEvent(encomenda.getId(), aluno.getEmail(), encomenda.getEstado()));

        LOG.info(() -> "Encomenda #" + encomenda.getId() + " paga por " + aluno.getEmail() + " (" + total + " €)");
        return new PagamentoDTO(EncomendaDTO.from(encomenda), aluno.getSaldo());
    }

    /** "Os meus pedidos" – o aluno só vê as suas encomendas. */
    @RolesAllowed(Papeis.ALUNO)
    public List<EncomendaDTO> minhas() {
        return em.createNamedQuery(Encomenda.DO_ALUNO, Encomenda.class)
                .setParameter("email", utilizador())
                .getResultList().stream()
                .map(EncomendaDTO::from)
                .toList();
    }

    /** RN4 – cancelar enquanto PAGA; repõe saldo e stock. Um aluno só cancela as suas. */
    @RolesAllowed(Papeis.ALUNO)
    public EncomendaDTO cancelar(Long id) {
        Encomenda encomenda = encontrar(id, LockModeType.PESSIMISTIC_WRITE);
        verificarDono(encomenda);

        if (!encomenda.getEstado().podeCancelar()) {
            throw new EstadoInvalidoException(
                    "Só é possível cancelar encomendas no estado PAGA (estado atual: " + encomenda.getEstado() + ").");
        }
        encomenda.mudarEstado(EstadoEncomenda.CANCELADA);
        encomenda.getAluno().creditar(encomenda.getTotal());
        for (LinhaEncomenda linha : encomenda.getLinhas()) {
            linha.getProduto().reporStock(linha.getQuantidade());
        }

        eventos.fire(new EncomendaAlteradaEvent(id, encomenda.getAluno().getEmail(), EstadoEncomenda.CANCELADA));
        LOG.info(() -> "Encomenda #" + id + " cancelada; repostos " + encomenda.getTotal() + " €");
        return EncomendaDTO.from(encomenda);
    }

    // ------------------------------------------------------- TODOS / BALCÃO

    /** Detalhe de uma encomenda: o aluno dono ou alguém do bar. */
    @RolesAllowed({Papeis.ALUNO, Papeis.FUNCIONARIO, Papeis.GESTOR})
    public EncomendaDTO obter(Long id) {
        Encomenda encomenda = encontrar(id, LockModeType.NONE);
        verificarDono(encomenda);
        return EncomendaDTO.from(encomenda);
    }

    /** US05 – fila do balcão: encomendas ativas do dia, por intervalo e por ordem de chegada. */
    @RolesAllowed({Papeis.FUNCIONARIO, Papeis.GESTOR})
    public List<EncomendaDTO> fila(LocalDate dia, Long intervaloId) {
        LocalDate data = dia != null ? dia : relogio.hoje();
        return em.createNamedQuery(Encomenda.FILA, Encomenda.class)
                .setParameter("inicio", data.atStartOfDay())
                .setParameter("fim", data.plusDays(1).atStartOfDay())
                .setParameter("estados", EstadoEncomenda.ATIVOS)
                .getResultList().stream()
                .filter(e -> intervaloId == null || intervaloId.equals(e.getIntervalo().getId()))
                .sorted(Comparator.comparing((Encomenda e) -> e.getIntervalo().getHoraInicio())
                        .thenComparing(Encomenda::getDataHora))
                .map(EncomendaDTO::from)
                .toList();
    }

    /** Funcionário muda o estado (EM_PREPARACAO → PRONTA → ENTREGUE). */
    @RolesAllowed({Papeis.FUNCIONARIO, Papeis.GESTOR})
    public EncomendaDTO mudarEstado(Long id, EstadoEncomenda novo) {
        if (novo == EstadoEncomenda.CANCELADA) {
            throw new EstadoInvalidoException("O cancelamento é feito pelo aluno (RN4).");
        }
        Encomenda encomenda = encontrar(id, LockModeType.PESSIMISTIC_WRITE);
        aplicarEstado(encomenda, novo);
        return EncomendaDTO.from(encomenda);
    }

    // -------------------------------------------------------------- SISTEMA

    /**
     * Chamado pelo MDB da cozinha (etapa 8): PAGA → EM_PREPARACAO.
     * Se entretanto o aluno cancelou, não faz nada.
     */
    @PermitAll
    public void iniciarPreparacao(Long id) {
        Encomenda encomenda = em.find(Encomenda.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (encomenda == null || encomenda.getEstado() != EstadoEncomenda.PAGA) {
            LOG.info(() -> "Cozinha: encomenda #" + id + " ignorada (inexistente ou já não está PAGA)");
            return;
        }
        aplicarEstado(encomenda, EstadoEncomenda.EM_PREPARACAO);
        LOG.info(() -> "Cozinha: encomenda #" + id + " → EM_PREPARACAO");
    }

    /**
     * RN5 – fecho do dia: encomendas PRONTA não levantadas passam a ENTREGUE, com registo.
     *
     * @return número de encomendas fechadas automaticamente
     */
    @PermitAll
    public int fecharDia(LocalDate dia) {
        List<Encomenda> prontas = em.createNamedQuery(Encomenda.PRONTAS_ATE, Encomenda.class)
                .setParameter("estado", EstadoEncomenda.PRONTA)
                .setParameter("limite", dia.plusDays(1).atStartOfDay())
                .getResultList();

        for (Encomenda e : prontas) {
            e.marcarEntregaAutomatica();
            LOG.warning(() -> "Fecho do dia: encomenda #" + e.getId() + " de " + e.getAluno().getNome()
                    + " não foi levantada → ENTREGUE (registo automático)");
            eventos.fire(new EncomendaAlteradaEvent(e.getId(), e.getAluno().getEmail(), EstadoEncomenda.ENTREGUE));
        }
        LOG.info(() -> "Fecho do dia " + dia + ": " + prontas.size() + " encomenda(s) fechadas automaticamente");
        return prontas.size();
    }

    // ------------------------------------------------------------- auxiliares

    private void aplicarEstado(Encomenda encomenda, EstadoEncomenda novo) {
        if (!encomenda.getEstado().podeMudarPara(novo)) {
            throw new EstadoInvalidoException(
                    "Não é possível passar de " + encomenda.getEstado() + " para " + novo + ".");
        }
        encomenda.mudarEstado(novo);
        eventos.fire(new EncomendaAlteradaEvent(encomenda.getId(), encomenda.getAluno().getEmail(), novo));
    }

    private Encomenda encontrar(Long id, LockModeType lock) {
        Encomenda encomenda = em.find(Encomenda.class, id, lock);
        if (encomenda == null) {
            throw new NaoEncontradoException("Encomenda", id);
        }
        return encomenda;
    }

    /** Segurança (etapa 10): um aluno só acede às suas encomendas. */
    private void verificarDono(Encomenda encomenda) {
        boolean staff = ctx.isCallerInRole(Papeis.FUNCIONARIO) || ctx.isCallerInRole(Papeis.GESTOR);
        if (!staff && !encomenda.getAluno().getEmail().equalsIgnoreCase(utilizador())) {
            throw new AcessoNegadoException("Esta encomenda não te pertence.");
        }
    }

    private Aluno alunoAutenticado(LockModeType lock) {
        List<Aluno> resultado = em.createNamedQuery(Aluno.POR_EMAIL, Aluno.class)
                .setParameter("email", utilizador())
                .setLockMode(lock)
                .getResultList();
        if (resultado.isEmpty()) {
            throw new NaoEncontradoException("Aluno", utilizador());
        }
        return resultado.get(0);
    }

    private String utilizador() {
        return ctx.getCallerPrincipal().getName();
    }
}
