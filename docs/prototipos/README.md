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
| `admin-panorama.png` | Administração (chefe): contadores, o que exige atenção, atalhos e ficha da instância |
| `admin-cadastros.png` | Administração: usuários, processos, prazos (com os já cumpridos), peças e anexos |
| `admin-relacionados.png` | Administração: clientes (com os desativados), partes contrárias, audiências e contratos |
| `admin-apoio.png` | Administração: modelos, feriados e os avisos emitidos na instância |

Regerar: com a aplicação em `localhost:8080` e Chrome headless em `--remote-debugging-port=9223`, rodar o script de captura (`shots.mjs`, Node 22+).

As quatro capturas da administração são recortes por seção da mesma página (`/painel/admin`), que é longa demais para uma imagem só. Cliente, parte contrária, audiência, contrato e anexo não entram nos dados de exemplo — foram criados pela API REST antes da captura, para as tabelas não aparecerem vazias.
