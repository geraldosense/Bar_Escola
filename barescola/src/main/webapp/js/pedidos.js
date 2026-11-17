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

function etapas(estadoAtual) {
  if (estadoAtual === 'CANCELADA') return '';
  const indice = ORDEM.indexOf(estadoAtual);
  return `<ol class="etapas" aria-label="Progresso do pedido">${ORDEM.map((e, i) => `
    <li class="${i <= indice ? 'feito' : ''} ${i === indice ? 'atual' : ''}"
        ${i === indice ? 'aria-current="step"' : ''}>${NOMES_ESTADO[e]}</li>`).join('')}</ol>`;
}

function senha(e) {
  const nota = e.entregaAutomatica ? '<p class="senha-meta">Fechada automaticamente no fim do dia.</p>' : '';
  return `
    <article class="senha estado-${e.estado} ${e.id === destaque ? 'acabou-de-ficar-pronta' : ''}" id="encomenda-${e.id}">
      <div class="senha-numero"><span>Senha</span><strong>${e.id}</strong></div>
      <div class="senha-corpo">
        <div class="senha-topo">
          <div>
            <h3>${esc(e.intervalo)}, ${hora(e.horaIntervalo)}</h3>
            <p class="senha-meta">Encomendada às ${horaDe(e.dataHora)}</p>
          </div>
          <span class="estado estado-${e.estado}">${NOMES_ESTADO[e.estado]}</span>
        </div>
        ${etapas(e.estado)}
        ${nota}
        <ul class="senha-itens">${e.linhas.map((l) => `
          <li><span><b>${l.quantidade}×</b>${esc(l.produto)}</span><span>${euros(l.subtotal)}</span></li>`).join('')}
        </ul>
        <div class="senha-rodape">
          <span>Total <span class="valor">${euros(e.total)}</span></span>
          ${e.podeCancelar ? `<button type="button" class="btn btn-perigo btn-pequeno" data-cancelar="${e.id}">Cancelar e devolver saldo</button>` : ''}
        </div>
      </div>
    </article>`;
}

function desenhar(encomendas) {
  if (!encomendas.length) {
    $('encomendas').innerHTML = `
      <div class="vazio-grande">
        <p>Ainda não tens encomendas. Escolhe no menu e paga com o cartão.</p>
        <a class="btn" href="menu.html">Abrir o menu</a>
      </div>`;
    return;
  }
  $('encomendas').innerHTML = encomendas.map(senha).join('');
}

async function cancelar(id) {
  if (!confirm(`Cancelar a senha ${id}? O valor volta para o teu cartão.`)) return;
  try {
    await api(`/encomendas/${id}/cancelar`, { method: 'POST' });
    toast(`Senha ${id} cancelada. O valor voltou para o teu cartão.`, 'sucesso');
    await Promise.all([carregar(), carregarUtilizador()]);
  } catch (e) {
    mostrarErro(e);
  }
}

/** Aviso vindo do servidor por WebSocket. */
function aoReceberAviso(msg) {
  if (msg.tipo === 'PEDIDO_PRONTO') {
    destaque = msg.encomendaId;
    toast(msg.mensagem, 'sucesso', 10000);
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
  $('ligacao').textContent = ligado ? 'Avisos ligados' : 'Sem ligação, a tentar de novo';
  $('ligacao').classList.toggle('ativa', ligado);
});

carregarUtilizador().catch(mostrarErro);
carregar();
