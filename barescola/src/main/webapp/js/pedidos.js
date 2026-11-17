/**
 * Os meus pedidos (US04): estado de cada encomenda, cancelamento (RN4) e
 * aviso "o teu pedido está pronto" em tempo real por WebSocket (etapa 7).
 */
import {
  NOMES_ESTADO, api, carregarUtilizador, esc, euros, hora, horaDe, ligarWebSocket, mostrarErro, toast,
} from './common.js';

const ORDEM = ['PAGA', 'EM_PREPARACAO', 'PRONTA', 'ENTREGUE'];
const $ = (id) => document.getElementById(id);
let destaque = null;

async function carregar() {
  try {
    const encomendas = await api('/encomendas/minhas');
    desenhar(encomendas);
  } catch (e) {
    mostrarErro(e);
  }
}

function progresso(estadoAtual) {
  if (estadoAtual === 'CANCELADA') return '';
  const indice = ORDEM.indexOf(estadoAtual);
  return `<div class="progresso" aria-hidden="true">${ORDEM.map((_, i) =>
    `<span class="${i <= indice ? 'feito' : ''}"></span>`).join('')}</div>`;
}

function desenhar(encomendas) {
  if (!encomendas.length) {
    $('encomendas').innerHTML = `
      <div class="cartao vazio-grande">
        <p>Ainda não fizeste nenhuma encomenda.</p>
        <a class="btn" href="menu.html">Ver o menu</a>
      </div>`;
    return;
  }
  $('encomendas').innerHTML = encomendas.map((e) => `
    <article class="cartao encomenda ${e.id === destaque ? 'destaque' : ''}" id="encomenda-${e.id}">
      <div class="cabecalho">
        <h2>Encomenda #${e.id}</h2>
        <span class="estado estado-${e.estado}">${NOMES_ESTADO[e.estado]}</span>
      </div>
      <p class="meta">
        ${esc(e.intervalo)} às ${hora(e.horaIntervalo)} · feita às ${horaDe(e.dataHora)}
        ${e.entregaAutomatica ? ' · fechada automaticamente no fim do dia' : ''}
      </p>
      ${progresso(e.estado)}
      <ul>${e.linhas.map((l) => `<li>${l.quantidade} × ${esc(l.produto)} — ${euros(l.subtotal)}</li>`).join('')}</ul>
      <div class="cabecalho">
        <strong>Total: ${euros(e.total)}</strong>
        <div class="acoes">
          ${e.podeCancelar ? `<button type="button" class="btn btn-perigo btn-pequeno" data-cancelar="${e.id}">Cancelar</button>` : ''}
        </div>
      </div>
    </article>`).join('');
}

async function cancelar(id) {
  if (!confirm(`Cancelar a encomenda #${id}? O valor volta para o teu cartão.`)) return;
  try {
    await api(`/encomendas/${id}/cancelar`, { method: 'POST' });
    toast(`Encomenda #${id} cancelada. O saldo foi reposto.`, 'sucesso');
    await Promise.all([carregar(), carregarUtilizador()]);
  } catch (e) {
    mostrarErro(e);
  }
}

/** Aviso vindo do servidor por WebSocket. */
function aoReceberAviso(msg) {
  if (msg.tipo === 'PEDIDO_PRONTO') {
    destaque = msg.encomendaId;
    toast(`🔔 ${msg.mensagem}`, 'sucesso', 10000);
    navigator.vibrate?.([200, 100, 200]);
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('BarEscola', { body: msg.mensagem });
    }
  } else if (msg.estado === 'EM_PREPARACAO') {
    toast(msg.mensagem);
  }
  carregar();
}

document.addEventListener('click', (ev) => {
  const botao = ev.target.closest('[data-cancelar]');
  if (botao) cancelar(Number(botao.dataset.cancelar));
});

if ('Notification' in window && Notification.permission === 'default') {
  document.addEventListener('click', () => Notification.requestPermission(), { once: true });
}

ligarWebSocket('/ws/aluno', aoReceberAviso, (ligado) => {
  $('ligacao').textContent = ligado ? 'Avisos em tempo real ativos' : 'Sem ligação – a tentar de novo…';
  $('ligacao').classList.toggle('ativa', ligado);
});

carregarUtilizador().catch(mostrarErro);
carregar();
