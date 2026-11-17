<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
  ETAPA 3 – Menu gerado no servidor (MenuServlet → esta JSP).
  O carrinho vive na HttpSession (classe Carrinho). Os DTO são records, por isso
  usa-se a sintaxe de método em EL: ${p.nome()}.
--%>
<fmt:setLocale value="pt_PT"/>
<!DOCTYPE html>
<html lang="pt">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Menu (JSP) | BarEscola</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
  <link rel="icon" type="image/svg+xml" href="/barescola/favicon.svg">
</head>
<body>
<header class="topo">
    <div class="topo-interior">
  <a class="marca" href="${pageContext.request.contextPath}/menu"><img src="${pageContext.request.contextPath}/img/logo/logo-branco.svg" alt="BarEscola" width="148" height="30"></a>
  <nav>
    <a href="${pageContext.request.contextPath}/menu" aria-current="page">Menu (versão JSP)</a>
    <a href="${pageContext.request.contextPath}/aluno/pedidos.html">Os meus pedidos</a>
  </nav>
  <div class="utilizador">
    <span class="nome"><c:out value="${pageContext.request.userPrincipal.name}"/></span>
    <form method="post" action="${pageContext.request.contextPath}/logout"><button class="btn-sair" type="submit"><svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg><span>Sair</span></button></form>
  </div>
  </div>
  </header>

<main class="conteudo">
  <h1>Menu do dia</h1>

  <c:if test="${not empty sucesso}"><div class="alerta alerta-sucesso"><c:out value="${sucesso}"/></div></c:if>
  <c:if test="${not empty erro}"><div class="alerta alerta-erro"><c:out value="${erro}"/></div></c:if>

  <div class="grelha-menu">
    <section class="produtos">
      <c:forEach var="p" items="${produtos}">
        <article class="produto ${p.disponivel() ? '' : 'indisponivel'}">
          <span class="categoria"><c:out value="${p.categoria()}"/></span>
          <span class="nome"><c:out value="${p.nome()}"/></span>
          <span class="stock">${p.disponivel() ? p.stock().toString().concat(' disponíveis') : 'Esgotado'}</span>
          <div class="rodape">
            <span class="preco"><fmt:formatNumber value="${p.preco()}" type="currency" currencyCode="EUR"/></span>
            <form method="post" action="${pageContext.request.contextPath}/carrinho">
              <input type="hidden" name="acao" value="adicionar">
              <input type="hidden" name="produtoId" value="${p.id()}">
              <button class="btn btn-redondo" ${p.disponivel() ? '' : 'disabled'} aria-label="Adicionar">+</button>
            </form>
          </div>
        </article>
      </c:forEach>
    </section>

    <aside class="cartao carrinho">
      <h2>Carrinho <span class="ajuda">${carrinho.numeroArtigos} artigo(s)</span></h2>
      <ul>
        <c:choose>
          <c:when test="${carrinho.vazio}">
            <li class="vazio">O carrinho está vazio.</li>
          </c:when>
          <c:otherwise>
            <c:forEach var="i" items="${carrinho.itens}">
              <li>
                <span><c:out value="${i.nome}"/><br>
                  <span class="subtotal">${i.quantidade} × <fmt:formatNumber value="${i.preco}" type="currency" currencyCode="EUR"/></span>
                </span>
                <form method="post" action="${pageContext.request.contextPath}/carrinho">
                  <input type="hidden" name="acao" value="remover">
                  <input type="hidden" name="produtoId" value="${i.produtoId}">
                  <button class="btn btn-secundario btn-redondo" aria-label="Remover">−</button>
                </form>
              </li>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </ul>
      <div class="total"><span>Total</span><span><fmt:formatNumber value="${carrinho.total}" type="currency" currencyCode="EUR"/></span></div>

      <form method="post" action="${pageContext.request.contextPath}/encomendar">
        <div class="campo">
          <label for="intervaloId">Levantar no intervalo</label>
          <select id="intervaloId" name="intervaloId" required>
            <c:forEach var="i" items="${intervalos}">
              <option value="${i.id()}" ${i.aberto() ? '' : 'disabled'}>
                <c:out value="${i.nome()}"/> · ${i.horaInicio()} ${i.aberto() ? '' : '(fechado)'}
              </option>
            </c:forEach>
          </select>
        </div>
        <button class="btn btn-bloco" ${carrinho.vazio ? 'disabled' : ''}>Encomendar e pagar</button>
      </form>
      <form method="post" action="${pageContext.request.contextPath}/carrinho" style="margin-top:.5rem">
        <input type="hidden" name="acao" value="limpar">
        <button class="btn btn-secundario btn-bloco" ${carrinho.vazio ? 'disabled' : ''}>Limpar carrinho</button>
      </form>
    </aside>
  </div>
</main>
</body>
</html>
