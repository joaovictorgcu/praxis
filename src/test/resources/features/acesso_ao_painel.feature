# language: pt
Funcionalidade: Acesso ao painel por advogados e chefes
  Para que a OAB de quem le uma peca seja a de quem realmente esta no sistema
  Como escritorio de advocacia
  Quero que advogados e chefes entrem com e-mail e senha e recebam as acoes do seu papel

  Cenario: chefe cadastrado entra com e-mail e senha
    Dado o usuario "Carla Mendes" com e-mail "carla@bdd.adv.br", OAB "PE90001", papel "CHEFE" e senha "chefe123"
    Quando eu entro com e-mail "CARLA@bdd.adv.br" e senha "chefe123"
    Entao o acesso deve ser concedido para a OAB "PE90001"
    E o usuario logado deve poder aprovar pecas

  Cenario: advogado entra mas nao aprova peca
    Dado o usuario "Ana Souza" com e-mail "ana@bdd.adv.br", OAB "PE90002", papel "ADVOGADO" e senha "ana12345"
    Quando eu entro com e-mail "ana@bdd.adv.br" e senha "ana12345"
    Entao o acesso deve ser concedido para a OAB "PE90002"
    E o usuario logado nao deve poder aprovar pecas

  Cenario: senha errada e e-mail desconhecido recebem a mesma recusa
    Dado o usuario "Bruno Carvalho" com e-mail "bruno@bdd.adv.br", OAB "PE90003", papel "ADVOGADO" e senha "bruno123"
    Quando eu entro com e-mail "bruno@bdd.adv.br" e senha "outra"
    Entao o acesso deve ser negado com "e-mail ou senha invalidos"
    Quando eu entro com e-mail "ninguem@bdd.adv.br" e senha "bruno123"
    Entao o acesso deve ser negado com "e-mail ou senha invalidos"

  Cenario: a senha nao fica guardada em texto
    Dado o usuario "Davi Lima" com e-mail "davi@bdd.adv.br", OAB "PE90004", papel "ADVOGADO" e senha "davi1234"
    Entao a senha guardada para "davi@bdd.adv.br" nao deve ser "davi1234"

  Cenario: e-mail e OAB sao unicos no escritorio
    Dado o usuario "Elisa Rocha" com e-mail "elisa@bdd.adv.br", OAB "PE90005", papel "ADVOGADO" e senha "elisa123"
    Entao cadastrar outro usuario com e-mail "elisa@bdd.adv.br" e OAB "PE90006" deve falhar
    E cadastrar outro usuario com e-mail "outra@bdd.adv.br" e OAB "PE90005" deve falhar
