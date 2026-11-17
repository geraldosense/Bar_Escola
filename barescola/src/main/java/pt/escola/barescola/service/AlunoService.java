package pt.escola.barescola.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.AlunoDTO;
import pt.escola.barescola.model.Aluno;
import pt.escola.barescola.service.exception.DadosInvalidosException;
import pt.escola.barescola.service.exception.NaoEncontradoException;

/** Alunos, saldo e carregamentos. */
@Stateless
@RolesAllowed(Papeis.GESTOR)
public class AlunoService {

    private static final Logger LOG = Logger.getLogger(AlunoService.class.getName());

    @PersistenceContext
    private EntityManager em;

    /** Aluno associado a um utilizador autenticado (o login é o email). */
    @PermitAll
    public Optional<AlunoDTO> porEmail(String email) {
        return em.createNamedQuery(Aluno.POR_EMAIL, Aluno.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .map(AlunoDTO::from);
    }

    /** Usado pelo serviço SOAP {@code CartaoWS} (sistema de cartões da escola). */
    @PermitAll
    public Optional<AlunoDTO> porNumeroCartao(int numero) {
        return em.createNamedQuery(Aluno.POR_NUMERO, Aluno.class)
                .setParameter("numero", numero)
                .getResultStream()
                .findFirst()
                .map(AlunoDTO::from);
    }

    public List<AlunoDTO> listar() {
        return em.createNamedQuery(Aluno.TODOS, Aluno.class).getResultList().stream()
                .map(AlunoDTO::from)
                .toList();
    }

    /** Carregamento de saldo (gestor). */
    public AlunoDTO carregarSaldo(int numero, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new DadosInvalidosException("O valor do carregamento tem de ser positivo.");
        }
        Aluno aluno = em.createNamedQuery(Aluno.POR_NUMERO, Aluno.class)
                .setParameter("numero", numero)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Aluno", numero));
        aluno.creditar(valor);
        LOG.info(() -> "Carregamento de " + valor + " € no cartão " + numero);
        return AlunoDTO.from(aluno);
    }
}
