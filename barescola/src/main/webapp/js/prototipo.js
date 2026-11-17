// ETAPA 2 – carrinho em JavaScript puro com dados falsos.
'use strict';

const PRODUTOS_FALSOS = [
  { id: 1, nome: 'Água 50 cl', categoria: 'Bebidas', preco: 0.6, stock: 40 },
  { id: 2, nome: 'Sumo de laranja', categoria: 'Bebidas', preco: 1.2, stock: 15 },
  { id: 3, nome: 'Sandes mista', categoria: 'Sandes', preco: 1.6, stock: 18 },
  { id: 4, nome: 'Tosta mista', categoria: 'Sandes', preco: 1.9, stock: 12 },
  { id: 5, nome: 'Maçã', categoria: 'Fruta', preco: 0.4, stock: 30 },
  { id: 6, nome: 'Banana', categoria: 'Fruta', preco: 0.4, stock: 0 },
];

const carrinho = {}; // id → quantidade
const formatar = (v) => v.toLocaleString('pt-PT', { style: 'currency', currency: 'EUR' });

function desenharProdutos() {
  document.getElementById('produtos').innerHTML = PRODUTOS_FALSOS.map((p) => `
    <article class="produto ${p.stock ? '' : 'indisponivel'}">
      <span class="categoria">${p.categoria}</span>
      <span class="nome">${p.nome}</span>
      <span class="stock">${p.stock ? p.stock + ' disponíveis' : 'Esgotado'}</span>
      <div class="rodape">
        <span class="preco">${formatar(p.preco)}</span>
        <button class="btn btn-redondo" data-id="${p.id}" ${p.stock ? '' : 'disabled'}>+</button>
      </div>
    </article>`).join('');
}

function desenharCarrinho() {
  let total = 0;
  const linhas = Object.entries(carrinho).map(([id, qtd]) => {
    const p = PRODUTOS_FALSOS.find((x) => x.id === Number(id));
    total += p.preco * qtd;
    return `<li><span>${qtd} × ${p.nome}</span>
      <button class="btn btn-secundario btn-redondo" data-remover="${id}">−</button></li>`;
  }).join('');
  document.getElementById('linhas').innerHTML = linhas || '<li class="vazio">Vazio</li>';
  document.getElementById('total').textContent = formatar(total);
  document.getElementById('barra-total').textContent = formatar(total);
}

document.addEventListener('click', (ev) => {
  const b = ev.target.closest('button');
  if (!b) return;
  if (b.dataset.id) carrinho[b.dataset.id] = (carrinho[b.dataset.id] || 0) + 1;
  if (b.dataset.remover) {
    carrinho[b.dataset.remover] -= 1;
    if (carrinho[b.dataset.remover] <= 0) delete carrinho[b.dataset.remover];
  }
  if (b.id === 'limpar') Object.keys(carrinho).forEach((k) => delete carrinho[k]);
  desenharCarrinho();
});

desenharProdutos();
desenharCarrinho();
