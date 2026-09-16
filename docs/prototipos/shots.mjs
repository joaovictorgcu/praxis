// Captura telas do praxis logado, via Chrome DevTools Protocol (WebSocket nativo do Node 22+).
import { writeFileSync } from 'node:fs';

const CDP = 'http://127.0.0.1:9223';
const BASE = 'http://localhost:8080';
const OUT = process.argv[2] || '.';
const largura = 1280;

const alvos = await (await fetch(`${CDP}/json`)).json();
const pagina = alvos.find(t => t.type === 'page') || (await (await fetch(`${CDP}/json/new?about:blank`, { method: 'PUT' })).json());
const ws = new WebSocket(pagina.webSocketDebuggerUrl);
await new Promise(r => ws.onopen = r);

let id = 0;
const pendentes = new Map();
const eventos = [];
ws.onmessage = ev => {
  const m = JSON.parse(ev.data);
  if (m.id && pendentes.has(m.id)) { pendentes.get(m.id)(m); pendentes.delete(m.id); }
  else if (m.method) eventos.push(m);
};
const enviar = (method, params = {}) => new Promise(res => { const i = ++id; pendentes.set(i, res); ws.send(JSON.stringify({ id: i, method, params })); });
const esperarCarga = async () => { for (let i = 0; i < 100; i++) { const r = await enviar('Runtime.evaluate', { expression: 'document.readyState', returnByValue: true }); if (r.result?.result?.value === 'complete') return; await new Promise(r => setTimeout(r, 100)); } };
const ir = async url => { await enviar('Page.navigate', { url: BASE + url }); await new Promise(r => setTimeout(r, 400)); await esperarCarga(); await new Promise(r => setTimeout(r, 300)); };
const js = async expr => (await enviar('Runtime.evaluate', { expression: expr, returnByValue: true, awaitPromise: true })).result?.result?.value;

await enviar('Page.enable');
await enviar('Emulation.setDeviceMetricsOverride', { width: largura, height: 900, deviceScaleFactor: 1, mobile: false });

async function foto(nome, alturaMax = 1400) {
  const altura = Math.min(alturaMax, Math.max(700, await js('document.documentElement.scrollHeight')));
  await enviar('Emulation.setDeviceMetricsOverride', { width: largura, height: altura, deviceScaleFactor: 1, mobile: false });
  await new Promise(r => setTimeout(r, 200));
  const r = await enviar('Page.captureScreenshot', { format: 'png', clip: { x: 0, y: 0, width: largura, height: altura, scale: 1 } });
  writeFileSync(`${OUT}/${nome}.png`, Buffer.from(r.result.data, 'base64'));
  console.log('ok', nome, `${largura}x${altura}`);
}

/** Recorte entre o topo de dois elementos: para pagina longa demais para uma imagem so. */
async function recorte(nome, seletorInicio, seletorFim) {
  const total = await js('document.documentElement.scrollHeight');
  await enviar('Emulation.setDeviceMetricsOverride', { width: largura, height: total, deviceScaleFactor: 1, mobile: false });
  await new Promise(r => setTimeout(r, 300));
  const topo = s => js(`document.querySelector('${s}').getBoundingClientRect().top + window.scrollY - 16`);
  const y = seletorInicio ? Math.max(0, await topo(seletorInicio)) : 0;
  const fim = seletorFim ? await topo(seletorFim) : await js('document.documentElement.scrollHeight');
  const r = await enviar('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true, clip: { x: 0, y: Math.round(y), width: largura, height: Math.round(fim - y), scale: 1 } });
  writeFileSync(`${OUT}/${nome}.png`, Buffer.from(r.result.data, 'base64'));
  console.log('ok', nome, `${largura}x${Math.round(fim - y)}`);
}

async function entrar(usuario, senha) {
  await ir('/login');
  await js(`document.querySelector('input[name=email]').value=${JSON.stringify(usuario)};document.querySelector('input[name=senha]').value=${JSON.stringify(senha)};true`);
  await js(`document.querySelector('form').submit();true`);
  await new Promise(r => setTimeout(r, 800)); await esperarCarga();
}
async function sair() {
  await js(`const f=document.querySelector('form[action="/sair"]'); f&&f.submit(); true`);
  await new Promise(r => setTimeout(r, 600));
}

// 1. Login
await ir('/login');
await foto('login', 720);

// 1b. Erro de login: e-mail desconhecido e senha errada recebem a mesma mensagem
await js(`document.querySelector('input[name=email]').value='admin';document.querySelector('input[name=senha]').value='senha-errada';document.querySelector('form').submit();true`);
await new Promise(r => setTimeout(r, 800)); await esperarCarga();
await foto('login-erro', 720);

/**
 * Cliente, parte contraria, audiencia, contrato e anexo nao entram nos dados de exemplo:
 * sao criados pela API REST (com a sessao e o CSRF do navegador) para as tabelas da
 * administracao nao aparecerem vazias nas capturas.
 */
async function semear() {
  const csrf = await js(`document.querySelector('input[name=_csrf]').value`);
  return js(`(async () => {
    const json = (url, corpo) => fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-Token': '${csrf}' }, body: JSON.stringify(corpo) }).then(r => r.status);
    const r = [];
    r.push(await json('/api/clientes', { nome: 'Construtora Alfa Ltda.', cpfOuCnpj: '12.345.678/0001-90', tipoPessoa: 'JURIDICA', email: 'contato@alfa.com.br', telefone: '(81) 3333-1010', cidade: 'Recife', estado: 'PE' }));
    r.push(await json('/api/clientes', { nome: 'Helena Barros', cpfOuCnpj: '123.456.789-09', tipoPessoa: 'FISICA', email: 'helena.barros@email.com', cidade: 'Olinda', estado: 'PE' }));
    r.push(await json('/api/partes-contrarias', { nome: 'Imobiliaria Beta ME', cpfOuCnpj: '98.765.432/0001-10', tipoPessoa: 'JURIDICA', cidade: 'Recife', estado: 'PE' }));
    r.push(await json('/api/audiencias', { numeroProcesso: '0001234-56.2026.8.17.0001', nomeParteAutora: 'Construtora Alfa Ltda.', dataHoraInicio: '2026-10-08T14:00:00', dataHoraFim: '2026-10-08T15:30:00', sala: 'Sala 3 - 2a Vara Civel' }));
    r.push(await json('/api/honorarios', { numeroProcesso: '0001234-56.2026.8.17.0001', modalidade: 'QUOTA_LITIS', celebradoEm: '2026-08-20', valorCausa: 120000, percentualExito: 20, horasTrabalhadas: 0 }));
    r.push(await json('/api/honorarios', { numeroProcesso: '0007654-32.2026.8.17.0002', modalidade: 'FIXO', celebradoEm: '2026-09-01', valorFixo: 8500, horasTrabalhadas: 0 }));
    const pdf = new Blob(['%PDF-1.4\\n% laudo pericial de exemplo\\n'], { type: 'application/pdf' });
    const form = new FormData();
    form.append('numeroProcesso', '0001234-56.2026.8.17.0001');
    form.append('arquivo', pdf, 'laudo-pericial.pdf');
    form.append('descricao', 'Laudo pericial do engenheiro');
    r.push(await fetch('/api/anexos', { method: 'POST', headers: { 'X-CSRF-Token': '${csrf}' }, body: form }).then(x => x.status));
    return r.join(',');
  })()`);
}

// 2. Chefe
await entrar('admin', '123');
console.log('semeadura (status HTTP):', await semear());
await ir('/painel');
await foto('painel-agenda');
await ir('/painel/processos'); await foto('processos');
await ir('/painel/processos/0001234-56.2026.8.17.0001'); await foto('processo-ficha', 1800);
await ir('/painel/documentos');
// escolhe o modelo para mostrar os campos gerados
await js(`const s=document.getElementById('codigo-modelo'); s.value='COBRANCA_ALUGUEL'; s.dispatchEvent(new Event('change')); true`);
await new Promise(r => setTimeout(r, 200));
await foto('documentos-gerar', 720);
await ir('/painel/documentos/1'); await foto('documento-aprovacao', 1500);
await ir('/painel/modelos');
await js(`const c=document.getElementById('corpo'); c.value='DOS FATOS\\n{{cliente}} celebrou contrato em {{comarca}}; deve {{valorDivida}} desde {{dataInadimplencia}}.'; c.dispatchEvent(new Event('input')); true`);
await foto('modelos', 1100);
await ir('/painel/feriados'); await foto('feriados', 1100);
await ir('/painel/anexos'); await foto('anexos', 760);
await ir('/painel/usuarios'); await foto('usuarios', 900);
await ir('/painel/conta'); await foto('conta', 800);
// Administracao: pagina longa, capturada em quatro recortes por secao
await ir('/painel/admin');
await recorte('admin-panorama', null, '#usuarios');
await recorte('admin-cadastros', '#usuarios', '#clientes');
await recorte('admin-relacionados', '#clientes', '#modelos');
await recorte('admin-apoio', '#modelos', null);

// 3. Capturas que alteram o estado da carga de exemplo (ficam por ultimo)
// 3a. Varredura simulando uma data futura: prazo vencido e prazo vencendo hoje
await ir('/painel');
await js(`const f=document.querySelector('form[action="/painel/varredura"]'); f.querySelector('#hoje').value='2026-09-25'; f.submit(); true`);
await new Promise(r => setTimeout(r, 800)); await esperarCarga();
await foto('agenda-varredura', 1500);

// 3b. Peca aprovada pelo chefe: some Aprovar/Rejeitar, aparecem Protocolar e Desfazer
await ir('/painel/documentos/1');
await js(`const f=document.querySelector('form[action="/painel/documentos/1/aprovar"]'); f.querySelector('input[name=comentario]').value='De acordo. Protocolar ate a data do prazo.'; f.submit(); true`);
await new Promise(r => setTimeout(r, 800)); await esperarCarga();
await js(`const s=document.querySelector('.fluxo').closest('section'); s.id='peca'; s.nextElementSibling.id='lista'; true`);
await recorte('documento-aprovado', '#peca', '#lista');

// 3c. Usuario cadastrado pelo chefe nasce com senha provisoria
await ir('/painel/usuarios');
await js(`const f=document.querySelector('form[action="/painel/usuarios"]'); f.nome.value='Diego Prado'; f.email.value='diego.prado@praxis.adv.br'; f.oab.value='PE77777'; f.senha.value='provisoria1'; f.submit(); true`);
await new Promise(r => setTimeout(r, 800)); await esperarCarga();
await sair();

await entrar('diego.prado', 'provisoria1');
await foto('senha-provisoria', 620);
await sair();

// 4. Advogada sem OAB habilitada tenta ler peca sigilosa
await entrar('ana.souza', 'praxis123');
await ir('/painel/processos/0007654-32.2026.8.17.0002'); await foto('processo-sigiloso-sem-oab', 1000);
// 4b. Tela de chefe pedida na mao por advogada: 403 do SessaoInterceptor
await ir('/painel/admin'); await foto('sem-permissao', 420);
await sair();

ws.close();
