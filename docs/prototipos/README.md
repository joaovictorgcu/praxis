# Protótipos de alta fidelidade

Capturas da interface real (Chrome headless, 1280 px), logado como chefe (`admin`) e como advogada (`ana.souza`).

| Arquivo | Tela |
|---|---|
| `login.png` | Login por usuário curto ou e-mail |
| `painel-agenda.png` | Resumo do dia, agenda de prazos, avisos do motor |
| `processos.png` | Lista/busca e cadastro de processos |
| `processo-ficha.png` | Ficha: linha do tempo, prazos, peças, anexos, registrar andamento, abrir prazo |
| `documentos-gerar.png` | Gerar peça por modelo (campos gerados a partir dos `{{marcadores}}`) |
| `documento-aprovacao.png` | Peça em revisão: aprovar/rejeitar (chefe), OABs habilitadas, histórico |
| `modelos.png` | Cadastro de modelo com detecção dos campos que serão pedidos |
| `feriados.png` | Cadastro de feriados e consulta "corre prazo em" |
| `anexos.png` | Juntada e download de arquivos |
| `usuarios.png` | Gestão de usuários (chefe) |
| `conta.png` | Minha conta / troca de senha |
| `processo-sigiloso-sem-oab.png` | Advogada sem OAB habilitada em processo sigiloso |

Regerar: com a aplicação em `localhost:8080` e Chrome headless em `--remote-debugging-port=9223`, rodar o script de captura (`shots.mjs`, Node 22+).
