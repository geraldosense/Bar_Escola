/**
 * BarEscola – funções partilhadas pelas páginas (módulo ES).
 * Chamadas à API, formatação, avisos (toasts), cabeçalho e WebSockets.
 */

/** Context root da aplicação, ex.: "/barescola". */
export const BASE = '/' + location.pathname.split('/')[1];

const euro = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' });

/** 1.5 → "1,50 €" */
export const euros = (valor) => euro.format(Number(valor ?? 0));

/** "10:05:00" → "10:05" */
export const hora = (texto) => (texto ?? '').substring(0, 5);

/** "2026-11-17T10:03:12" → "10:03" */
export const horaDe = (dataHora) => (dataHora ?? '').substring(11, 16);

export const NOMES_ESTADO = {
  PAGA: 'Paga',
  EM_PREPARACAO: 'Em preparação',
  PRONTA: 'Pronta',
  ENTREGUE: 'Entregue',
  CANCELADA: 'Cancelada',
};

/** Escapa texto antes de o pôr em innerHTML (evita XSS). */
export function esc(texto) {
  return String(texto ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  })[c]);
}

/** Erro da API com o JSON de erro do servidor ({status, codigo, mensagem, detalhes}). */
export class ErroApi extends Error {
  constructor(status, corpo) {
    super(corpo?.mensagem || `Erro ${status}`);
    this.status = status;
    this.codigo = corpo?.codigo;
    this.detalhes = corpo?.detalhes ?? [];
  }
}

/**
 * fetch com JSON, cookies de sessão e tratamento de erros.
 * @param {string} caminho ex.: "/produtos"
 * @param {{method?: string, body?: any}} opcoes
 */
export async function api(caminho, { method = 'GET', body } = {}) {
  const resposta = await fetch(`${BASE}/api${caminho}`, {
    method,
    credentials: 'same-origin',
    headers: {
      Accept: 'application/json',
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // Sessão expirada: o servidor devolve a página de login em vez de JSON
  const tipo = resposta.headers.get('Content-Type') ?? '';
  if (resposta.redirected && resposta.url.includes('login.html')) {
    location.href = `${BASE}/login.html`;
    throw new ErroApi(401, { mensagem: 'Sessão expirada' });
  }

  if (resposta.status === 204) return null;

  const corpo = tipo.includes('application/json') ? await resposta.json() : null;
  if (!resposta.ok) {
    throw new ErroApi(resposta.status, corpo ?? { mensagem: `Erro ${resposta.status}` });
  }
  return corpo;
}

/* ------------------------------------------------------------------ Toasts */

function contentorToasts() {
  let el = document.querySelector('.toasts');
  if (!el) {
    el = document.createElement('div');
    el.className = 'toasts';
    el.setAttribute('role', 'status');
    el.setAttribute('aria-live', 'polite');
    document.body.append(el);
  }
  return el;
}

/** Mostra uma mensagem temporária. tipo: "sucesso" | "erro" | "" */
export function toast(mensagem, tipo = '', duracaoMs = 4000) {
  const t = document.createElement('div');
  t.className = `toast ${tipo}`;
  t.textContent = mensagem;
  contentorToasts().append(t);
  setTimeout(() => t.remove(), duracaoMs);
}

/** Mostra o erro de uma chamada à API. */
export function mostrarErro(erro) {
  const extra = erro.detalhes?.length ? ` (${erro.detalhes.join('; ')})` : '';
  toast(erro.message + extra, 'erro', 6000);
}

/* --------------------------------------------------------------- Cabeçalho */

/**
 * Carrega o utilizador autenticado e preenche o cabeçalho (nome, saldo).
 * @returns {Promise<{username: string, papel: string, aluno: object|null}>}
 */
export async function carregarUtilizador() {
  const eu = await api('/me');
  const nome = document.querySelector('[data-utilizador-nome]');
  if (nome) nome.textContent = eu.aluno?.nome ?? eu.username;
  atualizarSaldo(eu.aluno?.saldo);
  return eu;
}

export function atualizarSaldo(saldo) {
  const el = document.querySelector('[data-saldo]');
  if (el && saldo !== undefined && saldo !== null) {
    el.textContent = euros(saldo);
    el.hidden = false;
  }
}

/* --------------------------------------------------------------- WebSocket */

/**
 * Liga a um endpoint WebSocket da aplicação e volta a ligar se cair.
 * @param {string} caminho "/ws/aluno" ou "/ws/balcao"
 * @param {(msg: object) => void} aoReceber
 * @param {(ligado: boolean) => void} [aoMudarEstado]
 */
export function ligarWebSocket(caminho, aoReceber, aoMudarEstado = () => {}) {
  const protocolo = location.protocol === 'https:' ? 'wss' : 'ws';
  let tentativas = 0;

  const ligar = () => {
    const ws = new WebSocket(`${protocolo}://${location.host}${BASE}${caminho}`);
    ws.onopen = () => { tentativas = 0; aoMudarEstado(true); };
    ws.onmessage = (ev) => {
      try { aoReceber(JSON.parse(ev.data)); } catch (e) { console.warn('Mensagem WS inválida', e); }
    };
    ws.onclose = () => {
      aoMudarEstado(false);
      const espera = Math.min(30000, 1000 * 2 ** tentativas++);
      setTimeout(ligar, espera);
    };
  };
  ligar();
}
