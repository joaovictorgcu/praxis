# Protótipos de alta fidelidade

Capturas da interface real (Chrome headless, 1280 px), logado como chefe (`admin`), como advogada (`ana.souza`) e como um usuário recém-criado, ainda com senha provisória.

| Arquivo | Tela |
|---|---|
| `login.png` | Login por usuário curto ou e-mail |
| `login-erro.png` | Login recusado: e-mail desconhecido e senha errada dão a mesma mensagem |
| `painel-agenda.png` | Resumo do dia, agenda de prazos, avisos do motor |
| `agenda-varredura.png` | Varredura simulando data futura: prazo vencido e os alertas ATENCAO e VENCIDO |
| `processos.png` | Lista/busca e cadastro de processos |
| `processo-ficha.png` | Ficha: linha do tempo, prazos, peças, anexos, registrar andamento, abrir prazo |
| `documentos-gerar.png` | Gerar peça por modelo (campos gerados a partir dos `{{marcadores}}`) |
| `documento-aprovacao.png` | Peça em revisão: aprovar/rejeitar (chefe), OABs habilitadas, histórico |
| `documento-aprovado.png` | Peça aprovada: Protocolar, Desfazer decisão e o histórico das transições |
| `modelos.png` | Cadastro de modelo com detecção dos campos que serão pedidos |
| `feriados.png` | Cadastro de feriados e consulta "corre prazo em" |
| `anexos.png` | Juntada e download de arquivos |
| `usuarios.png` | Gestão de usuários (chefe) |
| `conta.png` | Minha conta / troca de senha |
| `senha-provisoria.png` | Primeiro acesso com senha provisória: só *Minha conta* liberada |
| `processo-sigiloso-sem-oab.png` | Advogada sem OAB habilitada em processo sigiloso |
| `sem-permissao.png` | 403 do `SessaoInterceptor`: tela de chefe pedida por advogada |
| `admin-panorama.png` | Administração (chefe): contadores, o que exige atenção, atalhos e ficha da instância |
| `admin-cadastros.png` | Administração: usuários, processos, prazos (com os já cumpridos), peças e anexos |
| `admin-relacionados.png` | Administração: clientes (com os desativados), partes contrárias, audiências e contratos |
| `admin-apoio.png` | Administração: modelos, feriados e os avisos emitidos na instância |

## Regerar

1. Suba a aplicação limpa (`./mvnw spring-boot:run`) — o script cria dados e altera estado, então convém partir da carga de exemplo recém-carregada.
2. Suba o Chrome headless com `--remote-debugging-port=9223`.
3. Rode `node shots.mjs` (Node 22+) dentro desta pasta.

Detalhes do script:

- Cliente, parte contrária, audiência, contrato de honorário e anexo **não entram na carga de exemplo**: `semear()` os cria pela API REST, usando a sessão e o token CSRF do próprio navegador, para as tabelas da administração não aparecerem vazias.
- As quatro capturas da administração são recortes por seção da mesma página (`/painel/admin`), longa demais para uma imagem só.
- As capturas que **alteram estado** (varredura, aprovação da peça, criação de usuário) ficam no fim do roteiro, para não contaminar as anteriores.
