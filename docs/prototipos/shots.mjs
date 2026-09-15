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

// 2. Chefe
await entrar('admin', '123');
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
await sair();

// 3. Advogada sem OAB habilitada tenta ler peca sigilosa
await entrar('ana.souza', 'praxis123');
await ir('/painel/processos/0007654-32.2026.8.17.0002'); await foto('processo-sigiloso-sem-oab', 1000);
await sair();

ws.close();
