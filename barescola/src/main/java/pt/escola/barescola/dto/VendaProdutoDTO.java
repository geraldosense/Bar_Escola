package pt.escola.barescola.dto;

import java.io.Serializable;

import java.math.BigDecimal;

/** Resultado da consulta de vendas agrupada por produto (SUM). Usado num "SELECT new" JPQL. */
public record VendaProdutoDTO(String produto, Long quantidade, BigDecimal total) implements Serializable {
}
