/**
 * Menu do dia + carrinho + pagamento (US01, US02, US03).
 * Etapa 6: os dados vêm da API REST com fetch (na etapa 2 eram falsos – ver /prototipo).
 */
import {
  api, atualizarSaldo, carregarUtilizador, esc, euros, hora, mostrarErro, toast,
} from './common.js';

const CHAVE_CARRINHO = 'barescola.carrinho';
const MAX_POR_PRODUTO = 20;

const estado = {
  produtos: [],
  intervalos: [],
  categoria: 'Todas',
  /** @type {Map<number, number>} produtoId → quantidade */
  carrinho: new Map(JSON.parse(sessionStorage.getItem(CHAVE_CARRINHO) ?? '[]')),
};

const $ = (id) => document.getElementById(id);

/* ------------------------------------------------------------------ Dados */

async function carregar() {
  try {
    const [, produtos, intervalos] = await Promise.all([
      carregarUtilizador(),
      api('/produtos?ativos=true'),
      api('/intervalos'),
    ]);
    estado.produtos = produtos;
    estado.intervalos = intervalos;
    limparCarrinhoInvalido();
    desenharFiltros();
    desenharIntervalos();
    desenhar();
  } catch (e) {
    $('produtos').innerHTML = '<p class="vazio-grande">Não foi possível carregar o menu.</p>';
    mostrarErro(e);
  }
}

/** Remove do carrinho produtos que deixaram de existir ou de ter stock. */
function limparCarrinhoInvalido() {
  for (const [id, qtd] of estado.carrinho) {
    const p = produto(id);
    if (!p || !p.disponivel) estado.carrinho.delete(id);
    else if (qtd > p.stock) estado.carrinho.set(id, p.stock);
  }
  guardarCarrinho();
}

const produto = (id) => estado.produtos.find((p) => p.id === id);

/* --------------------------------------------------------------- Carrinho */

function alterarQuantidade(id, delta) {
  const p = produto(id);
  if (!p) return;
  const atual = estado.carrinho.get(id) ?? 0;
  const nova = Math.max(0, Math.min(atual + delta, p.stock, MAX_POR_PRODUTO));
  if (delta > 0 && nova === atual) {
    toast(`Não há mais stock de ${p.nome}.`, 'erro');
    return;
  }
  if (nova === 0) estado.carrinho.delete(id);
  else estado.carrinho.set(id, nova);
  guardarCarrinho();
  desenharCarrinho();
}

function guardarCarrinho() {
  sessionStorage.setItem(CHAVE_CARRINHO, JSON.stringify([...estado.carrinho]));
}

function totalCarrinho() {
  let total = 0;
  let artigos = 0;
  for (const [id, qtd] of estado.carrinho) {
    total += Number(produto(id)?.preco ?? 0) * qtd;
    artigos += qtd;
  }
  return { total, artigos };
}

/* -------------------------------------------------------------- Pagamento */

async function pagar() {
  const intervaloId = Number($('intervalo').value);
  if (!intervaloId) {
    toast('Escolhe o intervalo em que vais levantar.', 'erro');
    return;
  }
  const linhas = [...estado.carrinho].map(([produtoId, quantidade]) => ({ produtoId, quantidade }));

  const botao = $('btn-pagar');
  botao.disabled = true;
  botao.textContent = 'A pagar…';
  try {
    const pagamento = await api('/encomendas', { method: 'POST', body: { intervaloId, linhas } });
    estado.carrinho.clear();
    guardarCarrinho();
    atualizarSaldo(pagamento.saldoRestante);
    toast(`Encomenda #${pagamento.encomenda.id} paga! Saldo restante: ${euros(pagamento.saldoRestante)}`, 'sucesso', 6000);
    estado.produtos = await api('/produtos?ativos=true'); // stock atualizado
    desenhar();
  } catch (e) {
    mostrarErro(e);
  } finally {
    botao.textContent = 'Pagar com o cartão';
    desenharCarrinho();
  }
}

/* --------------------------------------------------------------- Desenho */

function desenharFiltros() {
  const categorias = ['Todas', ...new Set(estado.produtos.map((p) => p.categoria))];
  $('filtros').innerHTML = categorias.map((c) => `
    <button type="button" class="chip" data-categoria="${esc(c)}"
            aria-pressed="${c === estado.categoria}">${esc(c)}</button>`).join('');
}

function desenharIntervalos() {
  const abertos = estado.intervalos.filter((i) => i.aberto);
  $('intervalo').innerHTML = estado.intervalos.map((i) => `
    <option value="${i.id}" ${i.aberto ? '' : 'disabled'}>
      ${esc(i.nome)} · ${hora(i.horaInicio)} ${i.aberto ? `(até ${hora(i.horaFechoEncomendas)})` : '— fechado'}
    </option>`).join('');
  if (abertos.length) $('intervalo').value = abertos[0].id;
  else $('intervalo').insertAdjacentHTML('afterbegin', '<option value="" selected>Hoje já não há intervalos abertos</option>');
}

function desenharProdutos() {
  const visiveis = estado.produtos.filter((p) => estado.categoria === 'Todas' || p.categoria === estado.categoria);
  if (!visiveis.length) {
    $('produtos').innerHTML = '<p class="vazio-grande">Sem produtos nesta categoria.</p>';
    return;
  }
  $('produtos').innerHTML = visiveis.map((p) => `
    <article class="produto ${p.disponivel ? '' : 'indisponivel'}">
      <span class="categoria">${esc(p.categoria)}</span>
      <span class="nome">${esc(p.nome)}</span>
      <span class="stock">${p.disponivel ? `${p.stock} disponíveis` : 'Esgotado'}</span>
      <div class="rodape">
        <span class="preco">${euros(p.preco)}</span>
        <button type="button" class="btn btn-redondo" data-adicionar="${p.id}"
                ${p.disponivel ? '' : 'disabled'} aria-label="Adicionar ${esc(p.nome)}">+</button>
      </div>
    </article>`).join('');
}

function desenharCarrinho() {
  const { total, artigos } = totalCarrinho();
  const linhas = [...estado.carrinho].map(([id, qtd]) => {
    const p = produto(id);
    return `
      <li>
        <span>${esc(p.nome)}<br><span class="subtotal">${qtd} × ${euros(p.preco)} = ${euros(p.preco * qtd)}</span></span>
        <span class="qtd">
          <button type="button" class="btn btn-secundario btn-redondo" data-remover="${id}" aria-label="Menos ${esc(p.nome)}">−</button>
          <strong>${qtd}</strong>
          <button type="button" class="btn btn-secundario btn-redondo" data-adicionar="${id}" aria-label="Mais ${esc(p.nome)}">+</button>
        </span>
      </li>`;
  }).join('');

  $('carrinho-linhas').innerHTML = linhas || '<li class="vazio">O carrinho está vazio.</li>';
  $('carrinho-total').textContent = euros(total);
  $('carrinho-contador').textContent = artigos ? `${artigos} artigo${artigos > 1 ? 's' : ''}` : '';
  $('barra-total').textContent = euros(total);
  $('barra-artigos').textContent = `${artigos} artigo${artigos === 1 ? '' : 's'}`;
  $('btn-pagar').disabled = artigos === 0 || !$('intervalo').value;
}

function desenhar() {
  desenharProdutos();
  desenharCarrinho();
}

/* --------------------------------------------------------------- Eventos */

document.addEventListener('click', (ev) => {
  const alvo = ev.target.closest('button');
  if (!alvo) return;
  if (alvo.dataset.adicionar) alterarQuantidade(Number(alvo.dataset.adicionar), +1);
  else if (alvo.dataset.remover) alterarQuantidade(Number(alvo.dataset.remover), -1);
  else if (alvo.dataset.categoria) {
    estado.categoria = alvo.dataset.categoria;
    desenharFiltros();
    desenharProdutos();
  } else if (alvo.id === 'btn-pagar') pagar();
});

document.addEventListener('change', (ev) => {
  if (ev.target.id === 'intervalo') desenharCarrinho();
});

carregar();
