/**
 * Ecrã do balcão (US05): encomendas por intervalo e por ordem de chegada.
 * O funcionário avança o estado; o aluno recebe o aviso por WebSocket.
 */
import {
  NOMES_ESTADO, api, carregarUtilizador, esc, euros, hora, horaDe, ligarWebSocket, mostrarErro, toast,
} from './common.js';

const $ = (id) => document.getElementById(id);
const PROXIMO = {
  PAGA: { estado: 'EM_PREPARACAO', rotulo: 'Começar a preparar' },
  EM_PREPARACAO: { estado: 'PRONTA', rotulo: 'Marcar pronta' },
  PRONTA: { estado: 'ENTREGUE', rotulo: 'Entregue' },
};

let fila = [];

async function carregarIntervalos() {
  const intervalos = await api('/intervalos');
  $('filtro-intervalo').insertAdjacentHTML('beforeend', intervalos.map((i) =>
    `<option value="${i.id}">${esc(i.nome)} · ${hora(i.horaInicio)}</option>`).join(''));
}

async function carregar() {
  const intervaloId = $('filtro-intervalo').value;
  try {
    fila = await api(`/encomendas${intervaloId ? `?intervaloId=${intervaloId}` : ''}`);
    desenhar();
  } catch (e) {
    mostrarErro(e);
  }
}

function cartaoEncomenda(e) {
  const proximo = PROXIMO[e.estado];
  return `
    <article class="cartao encomenda">
      <div class="cabecalho">
        <h3>#${e.id} · ${esc(e.alunoNome)} <span class="ajuda">(${esc(e.turma)})</span></h3>
        <span class="estado estado-${e.estado}">${NOMES_ESTADO[e.estado]}</span>
      </div>
      <p class="meta">${esc(e.intervalo)} ${hora(e.horaIntervalo)} · chegou às ${horaDe(e.dataHora)} · ${euros(e.total)}</p>
      <ul>${e.linhas.map((l) => `<li><strong>${l.quantidade}×</strong> ${esc(l.produto)}</li>`).join('')}</ul>
      ${proximo ? `<div class="acoes"><button type="button" class="btn btn-pequeno" data-id="${e.id}"
          data-estado="${proximo.estado}">${proximo.rotulo}</button></div>` : ''}
    </article>`;
}

function desenhar() {
  const preparar = fila.filter((e) => e.estado === 'PAGA' || e.estado === 'EM_PREPARACAO');
  const prontas = fila.filter((e) => e.estado === 'PRONTA');
  const vazio = (texto) => `<p class="vazio-grande">${texto}</p>`;

  $('col-preparar').innerHTML = preparar.map(cartaoEncomenda).join('') || vazio('Sem encomendas por preparar.');
  $('col-prontas').innerHTML = prontas.map(cartaoEncomenda).join('') || vazio('Nada à espera de ser levantado.');
  $('c-preparar').textContent = preparar.length;
  $('c-prontas').textContent = prontas.length;

  const porIntervalo = new Map();
  for (const e of fila) {
    const chave = `${e.intervalo} · ${hora(e.horaIntervalo)}`;
    porIntervalo.set(chave, (porIntervalo.get(chave) ?? 0) + 1);
  }
  $('resumo').innerHTML = porIntervalo.size
    ? `<table><tbody>${[...porIntervalo].map(([k, n]) =>
      `<tr><td>${esc(k)}</td><td class="num"><strong>${n}</strong></td></tr>`).join('')}</tbody></table>`
    : '<p class="ajuda">Sem encomendas ativas hoje.</p>';
}

async function mudarEstado(id, estado) {
  try {
    await api(`/encomendas/${id}/estado`, { method: 'PUT', body: { estado } });
    if (estado === 'PRONTA') toast(`Encomenda #${id} pronta – o aluno foi avisado.`, 'sucesso');
    await carregar();
  } catch (e) {
    mostrarErro(e);
    carregar();
  }
}

document.addEventListener('click', (ev) => {
  const botao = ev.target.closest('button[data-estado]');
  if (botao) {
    botao.disabled = true;
    mudarEstado(Number(botao.dataset.id), botao.dataset.estado);
  }
});
$('filtro-intervalo').addEventListener('change', carregar);

ligarWebSocket('/ws/balcao', (msg) => {
  if (msg.estado === 'PAGA') toast(`Nova encomenda #${msg.encomendaId}`);
  carregar();
}, (ligado) => {
  $('ligacao').textContent = ligado ? 'Tempo real ativo' : 'Sem ligação – a tentar de novo…';
  $('ligacao').classList.toggle('ativa', ligado);
});

// Rede de segurança: atualiza a cada 30 s mesmo sem WebSocket
setInterval(carregar, 30000);

carregarUtilizador().catch(mostrarErro);
carregarIntervalos().then(carregar).catch(mostrarErro);
