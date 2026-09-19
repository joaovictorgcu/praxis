# O que esta entrega ainda nao faz

Limites conhecidos, ditos abertamente - todos com um ponto de troca identificado no codigo.

---

- E-mail é registrado em log e memória (o `NotificadorEmail` é o ponto de troca por `JavaMailSender`).
- Observadores são reanexados pela camada de aplicação a cada carregamento do agregado — suficiente para instância única, não para escala horizontal.
- H2 em memória: os dados se perdem no shutdown. Trocar para PostgreSQL altera apenas `application.properties` (ou usa o perfil `prod` acima).
- A autenticação é de sessão HTTP, própria (sem Spring Security), e vale só para o painel: o `GET` da API REST segue aberto, recebendo a OAB de leitura na requisição. Não há recuperação de senha por e-mail: quem esquece pede ao chefe para recadastrar.
- O freio de força bruta do login é em memória, por instância.
- A deteccao de conflito de audiencia agora e do agregado `AgendaDeAudiencias`; o repositorio so entrega as candidatas da sala. A JPQL `encontrarConflitosDeHorario` saiu.
- O papel é binário (advogado/chefe); não há vínculo entre usuário e processo além da OAB, então qualquer advogado logado vê a agenda inteira do escritório.
- Cliente, parte contrária e audiência continuam sem tela de cadastro: a administração mostra os três, inclusive os desativados, mas criar, editar e desativar segue só pela API REST.
- A administração carrega tudo de uma vez, sem paginação nem filtro: cabe no volume de um escritório, não em base grande. Paginar afeta só `PanoramaAppService` e o template.
- Os avisos listados na administração são a fila em memória do `NotificadorPainel` (200 últimos, por instância): somem no restart e não são histórico.
- O foro dos feriados é único e vem de propriedade (`praxis.foro.*`), não de cada processo: o `Processo` guarda a comarca, mas não a UF. Feriado por processo exigiria derivar a UF do código do tribunal no número CNJ.
- O cadastro de feriados é mantido em memória pelo adaptador (`FeriadoRepositorioJpa`), porque o calendário pergunta dia a dia ao percorrer um prazo. A escrita descarta o cache — suficiente para instância única.
- O modelo de documento define corpo e pedidos, não a ordem das seções: o esqueleto é regra do domínio. Modelo que precise de estrutura própria exigiria nova subclasse de `GeradorDocumento`.
- Modelos não têm versão. Editar o modelo não afeta peça já gerada (o documento persiste o conteúdo), mas o histórico do próprio modelo não é guardado.
- Os campos do modelo são texto simples, sem tipo nem obrigatoriedade: campo esquecido sai como `(nome a preencher)` na peça, e não barra a geração.
- Anexos são guardados em BLOB no banco. Simplifica backup e transação (arquivo e metadados commitam juntos), mas não escala para volume alto — a troca por armazenamento de objetos afeta apenas `ArquivoRepositorioJpa`.
- O conteúdo do anexo não é inspecionado: confia-se no `Content-Type` declarado no upload. Um PDF renomeado passaria. Validar assinatura de arquivo (magic number) e antivírus fica para produção.
- Anexo não tem versão nem desentranhamento: a juntada é definitiva na tela.
- O arquivo `.cml` não foi validado com o plugin do Context Mapper nesta máquina (extensão não instalada).
