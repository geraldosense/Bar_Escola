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
    estado.produtos = ordenar(produtos);
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

/** Ordem alfabética portuguesa (a BD ordena "Água" depois de "Sumo"). */
const ordenar = (lista) => [...lista].sort((a, b) =>
  a.categoria.localeCompare(b.categoria, 'pt') || a.nome.localeCompare(b.nome, 'pt'));

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
  desenharProdutos();
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
  botao.textContent = 'A pagar...';
  try {
    const pagamento = await api('/encomendas', { method: 'POST', body: { intervaloId, linhas } });
    estado.carrinho.clear();
    guardarCarrinho();
    atualizarSaldo(pagamento.saldoRestante);
    toast(`Encomenda #${pagamento.encomenda.id} paga! Saldo restante: ${euros(pagamento.saldoRestante)}`, 'sucesso', 6000);
    estado.produtos = ordenar(await api('/produtos?ativos=true')); // stock atualizado
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
      ${esc(i.nome)}, ${hora(i.horaInicio)} ${i.aberto ? `(até ${hora(i.horaFechoEncomendas)})` : '(fechado)'}
    </option>`).join('');
  if (abertos.length) $('intervalo').value = abertos[0].id;
  else $('intervalo').insertAdjacentHTML('afterbegin', '<option value="" selected>Hoje já não há intervalos abertos</option>');
}

const POUCO_STOCK = 3;

/* Ilustração de cada produto (img/produtos/*.svg), escolhida pelo nome e, em último caso, pela categoria.
   Para um produto novo basta juntar aqui uma palavra-chave e o respetivo SVG. */
const ILUSTRACOES = [
  ['frango', 'frango'], ['pao de queijo', 'pao-queijo'], ['leite', 'leite-chocolate'], ['agua', 'agua'], ['sumo', 'sumo'],
  ['cafe', 'cafe'], ['tosta', 'tosta'], ['sandes', 'sandes'], ['maca', 'maca'], ['banana', 'banana'],
  ['croissant', 'croissant'], ['rissol', 'rissol'], ['salgad', 'rissol'],
];
const POR_CATEGORIA = { bebidas: 'bebida', sandes: 'sandes', fruta: 'maca', 'bolos e salgados': 'croissant' };
const semAcentos = (t) => t.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

/* Fotografias reais (Wikimedia Commons, licenças livres – ver creditos.html).
   Ordem de tentativa: cópia local em img/fotos/ (scripts/descarregar-fotos.sh) → original na internet → ilustração SVG. */
const FOTOS = {
  'agua': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Evian_Label-Free_Bottle.jpg/500px-Evian_Label-Free_Bottle.jpg',
  'sumo': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Orange_juice_1_edit1.jpg/500px-Orange_juice_1_edit1.jpg',
  'leite-chocolate': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Chocolate_milk.JPG/500px-Chocolate_milk.JPG',
  'cafe': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Cup_of_espresso_02.jpg/500px-Cup_of_espresso_02.jpg',
  'sandes': 'https://upload.wikimedia.org/wikipedia/commons/c/c6/Ham_sandwich.jpg',
  'tosta': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Grilled_ham_and_cheese_sandwich.jpg/500px-Grilled_ham_and_cheese_sandwich.jpg',
  'frango': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/Chicken_sandwich.jpg/500px-Chicken_sandwich.jpg',
  'maca': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Wet_apple_isolated_on_white.jpg/500px-Wet_apple_isolated_on_white.jpg',
  'banana': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/10/Banana_isolated_on_white.jpg/500px-Banana_isolated_on_white.jpg',
  'croissant': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dc/Croissants_au_beurre_%2818953292873%29.jpg/500px-Croissants_au_beurre_%2818953292873%29.jpg',
  'pao-queijo': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/Pao_de_queijo_brasil.jpg/500px-Pao_de_queijo_brasil.jpg',
  'rissol': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/Risole2.jpg/500px-Risole2.jpg',
};

function chaveImagem(p) {
  const nome = semAcentos(p.nome);
  const encontrada = ILUSTRACOES.find(([chave]) => nome.includes(chave));
  return encontrada ? encontrada[1] : (POR_CATEGORIA[semAcentos(p.categoria)] ?? 'produto');
}

/** Devolve o src inicial e as alternativas (separadas por |) para o caso de falhar. */
function imagem(p) {
  const chave = chaveImagem(p);
  const svg = `../img/produtos/${chave}.svg`;
  if (!FOTOS[chave]) return { src: svg, alternativas: '' };
  return { src: `../img/fotos/${chave}.jpg`, alternativas: `${FOTOS[chave]}|${svg}` };
}

// Quando uma imagem falha, passa à alternativa seguinte; quando carrega uma foto, ajusta o enquadramento.
document.addEventListener('error', (ev) => {
  const img = ev.target;
  if (!(img instanceof HTMLImageElement) || !img.dataset.alternativas) return;
  const [seguinte, ...resto] = img.dataset.alternativas.split('|');
  img.dataset.alternativas = resto.join('|');
  img.src = seguinte;
}, true);
document.addEventListener('load', (ev) => {
  const img = ev.target;
  if (img instanceof HTMLImageElement && img.closest('.foto')) {
    img.closest('.foto').classList.toggle('tem-foto', !img.currentSrc.endsWith('.svg'));
  }
}, true);

/** Classe de cor do fundo da ilustração, por categoria. */
const tomCategoria = (categoria) => `tom-${semAcentos(categoria).split(' ')[0]}`;

function linhaProduto(p) {
  const qtd = estado.carrinho.get(p.id) ?? 0;
  const aviso = !p.disponivel ? '<span class="selo selo-esgotado">Esgotado</span>'
    : p.stock <= POUCO_STOCK ? `<span class="selo">Só ${p.stock}</span>` : '';
  const controlo = !p.disponivel
    ? '<button type="button" class="btn btn-bloco" disabled>Indisponível</button>'
    : qtd === 0
      ? `<button type="button" class="btn btn-bloco" data-adicionar="${p.id}" aria-label="Adicionar ${esc(p.nome)} ao pedido">Adicionar</button>`
      : `<div class="quantidade">
           <button type="button" class="btn btn-secundario btn-redondo" data-remover="${p.id}" aria-label="Menos ${esc(p.nome)}">−</button>
           <strong aria-label="${qtd} no pedido">${qtd}</strong>
           <button type="button" class="btn btn-redondo" data-adicionar="${p.id}" aria-label="Mais ${esc(p.nome)}">+</button>
         </div>`;
  const img = imagem(p);
  return `
    <li class="cartao-produto ${p.disponivel ? '' : 'esgotado'} ${qtd ? 'no-carrinho' : ''}">
      <div class="foto ${tomCategoria(p.categoria)}">
        <img src="${img.src}" data-alternativas="${img.alternativas}" alt="${esc(p.nome)}" loading="lazy" width="160" height="120">
        ${aviso}
      </div>
      <div class="info">
        <span class="item-nome">${esc(p.nome)}</span>
        <span class="item-preco">${euros(p.preco)}</span>
      </div>
      <div class="acao">${controlo}</div>
    </li>`;
}

function desenharProdutos() {
  const visiveis = estado.produtos.filter((p) => estado.categoria === 'Todas' || p.categoria === estado.categoria);
  if (!visiveis.length) {
    $('produtos').innerHTML = '<p class="vazio-grande">Não há produtos nesta categoria hoje.</p>';
    return;
  }
  const porCategoria = new Map();
  for (const p of visiveis) {
    if (!porCategoria.has(p.categoria)) porCategoria.set(p.categoria, []);
    porCategoria.get(p.categoria).push(p);
  }
  $('produtos').innerHTML = [...porCategoria].map(([categoria, produtos]) => `
    <section class="menu-categoria">
      <h2>${esc(categoria)} <span>${produtos.length} ${produtos.length === 1 ? 'produto' : 'produtos'}</span></h2>
      <ul class="lista-produtos grelha">${produtos.map(linhaProduto).join('')}</ul>
    </section>`).join('');
}

function desenharCarrinho() {
  const { total, artigos } = totalCarrinho();
  const linhas = [...estado.carrinho].map(([id, qtd]) => {
    const p = produto(id);
    return `
      <li>
        <span>${esc(p.nome)}<span class="subtotal">${qtd} × ${euros(p.preco)} = ${euros(p.preco * qtd)}</span></span>
        <span class="qtd">
          <button type="button" class="btn btn-secundario btn-redondo" data-remover="${id}" aria-label="Menos ${esc(p.nome)}">−</button>
          <strong>${qtd}</strong>
          <button type="button" class="btn btn-secundario btn-redondo" data-adicionar="${id}" aria-label="Mais ${esc(p.nome)}">+</button>
        </span>
      </li>`;
  }).join('');

  $('carrinho-linhas').innerHTML = linhas || '<li class="vazio">Ainda não escolheste nada. Toca em + num produto do menu.</li>';
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
