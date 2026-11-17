package pt.escola.barescola.service;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.escola.barescola.config.Papeis;
import pt.escola.barescola.dto.AlunoDTO;
import pt.escola.barescola.dto.RegistoDTO;
import pt.escola.barescola.model.Aluno;
import pt.escola.barescola.service.exception.DadosInvalidosException;
import pt.escola.barescola.service.exception.NegocioException;

/**
 * Criação de conta pelo próprio aluno (página "Criar conta").
 * Cria o aluno na BD (saldo 0 €, carregado depois no bar) e a conta de login com o papel ALUNO.
 * Contas de funcionário e gestor continuam a ser criadas apenas pela administração.
 */
@Stateless
@PermitAll
public class RegistoService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ContasServidor contas;

    public AlunoDTO registar(RegistoDTO dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        String turma = dados.turma().replace(" ", "").toUpperCase(Locale.ROOT);
        validarPalavraPasse(dados.palavraPasse(), email);

        if (!em.createNamedQuery(Aluno.POR_EMAIL, Aluno.class).setParameter("email", email).getResultList().isEmpty()
                || contas.existe(email)) {
            throw new NegocioException("CONTA_EXISTENTE", 409, "Já existe uma conta com este email. Usa \"Entrar\".");
        }
        if (!em.createNamedQuery(Aluno.POR_NUMERO, Aluno.class).setParameter("numero", dados.numero())
                .getResultList().isEmpty()) {
            throw new NegocioException("CONTA_EXISTENTE", 409,
                    "O número de aluno " + dados.numero() + " já tem conta. Se não foste tu, fala com o bar.");
        }

        Aluno aluno = new Aluno(dados.numero(), dados.nome().trim(), turma, email, BigDecimal.ZERO);
        em.persist(aluno);
        em.flush(); // falhas da BD acontecem aqui, antes de criar o login

        // Último passo: se falhar, a exceção desfaz também o aluno criado acima (transação).
        contas.criar(email, dados.palavraPasse(), Papeis.ALUNO);
        return AlunoDTO.from(aluno);
    }

    private static void validarPalavraPasse(String senha, String email) {
        boolean letra = senha.chars().anyMatch(Character::isLetter);
        boolean digito = senha.chars().anyMatch(Character::isDigit);
        if (!letra || !digito) {
            throw new DadosInvalidosException("A palavra-passe tem de ter letras e números.");
        }
        if (senha.toLowerCase(Locale.ROOT).contains(email.substring(0, email.indexOf('@')))) {
            throw new DadosInvalidosException("A palavra-passe não pode conter o teu email.");
        }
    }
}
