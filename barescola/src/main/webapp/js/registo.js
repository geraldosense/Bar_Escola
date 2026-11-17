// Criação de conta de aluno (POST /api/registo)
'use strict';

const $ = (id) => document.getElementById(id);
const REGRAS = {
  tamanho: (s) => s.length >= 8,
  letra: (s) => /\p{L}/u.test(s),
  numero: (s) => /\d/.test(s),
};

function mostrarErro(texto, detalhes = []) {
  const caixa = $('erro');
  caixa.innerHTML = '';
  caixa.append(document.createTextNode(texto));
  if (detalhes.length) {
    const ul = document.createElement('ul');
    detalhes.forEach((d) => {
      const li = document.createElement('li');
      li.textContent = d.replace(/^[a-zA-Z]+: /, '');
      ul.append(li);
    });
    caixa.append(ul);
  }
  caixa.hidden = false;
  caixa.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function atualizarRegras() {
  const senha = $('senha').value;
  document.querySelectorAll('[data-regra]').forEach((li) => {
    li.classList.toggle('cumprida', REGRAS[li.dataset.regra](senha));
  });
}

document.querySelectorAll('.ver-senha').forEach((botao) => {
  botao.addEventListener('click', () => {
    const campo = $(botao.dataset.alvo);
    const mostrar = campo.type === 'password';
    campo.type = mostrar ? 'text' : 'password';
    const texto = mostrar ? 'Ocultar palavra-passe' : 'Mostrar palavra-passe';
    botao.setAttribute('aria-pressed', String(mostrar));
    botao.setAttribute('aria-label', texto);
    botao.title = texto;
  });
});

$('senha').addEventListener('input', atualizarRegras);

$('form-registo').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  $('erro').hidden = true;

  const form = ev.target;
  if (!form.checkValidity()) {
    mostrarErro('Preenche todos os campos assinalados.');
    form.reportValidity();
    return;
  }
  const senha = $('senha').value;
  if (!Object.values(REGRAS).every((regra) => regra(senha))) {
    mostrarErro('A palavra-passe não cumpre as regras indicadas.');
    return;
  }
  if (senha !== $('senha2').value) {
    mostrarErro('As palavras-passe não coincidem.');
    $('senha2').focus();
    return;
  }

  const botao = $('btn-criar');
  botao.disabled = true;
  botao.textContent = 'A criar conta...';
  try {
    const resposta = await fetch('/barescola/api/registo', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({
        nome: $('nome').value.trim(),
        numero: Number($('numero').value),
        turma: $('turma').value.trim(),
        email: $('email').value.trim(),
        palavraPasse: senha,
      }),
    });
    const corpo = await resposta.json().catch(() => null);
    if (!resposta.ok) {
      mostrarErro(corpo?.mensagem ?? `Não foi possível criar a conta (erro ${resposta.status}).`, corpo?.detalhes ?? []);
      return;
    }
    $('msg-sucesso').textContent = `Já podes entrar com ${corpo.email}.`;
    $('link-entrar').href = `/barescola/login.html?email=${encodeURIComponent(corpo.email)}`;
    $('passo-formulario').hidden = true;
    $('passo-sucesso').hidden = false;
    form.reset();
  } catch {
    mostrarErro('Sem ligação ao servidor. Tenta outra vez.');
  } finally {
    botao.disabled = false;
    botao.textContent = 'Criar conta';
  }
});
