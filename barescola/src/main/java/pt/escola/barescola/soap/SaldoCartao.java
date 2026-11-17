package pt.escola.barescola.soap;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/** Resposta do CartaoWS.saldo. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saldoCartao", propOrder = {"numeroCartao", "nome", "turma", "saldo"})
public class SaldoCartao {

    private int numeroCartao;
    private String nome;
    private String turma;
    private BigDecimal saldo;

    public SaldoCartao() {
        // JAXB
    }

    public SaldoCartao(int numeroCartao, String nome, String turma, BigDecimal saldo) {
        this.numeroCartao = numeroCartao;
        this.nome = nome;
        this.turma = turma;
        this.saldo = saldo;
    }

    public int getNumeroCartao() { return numeroCartao; }

    public String getNome() { return nome; }

    public String getTurma() { return turma; }

    public BigDecimal getSaldo() { return saldo; }
}
