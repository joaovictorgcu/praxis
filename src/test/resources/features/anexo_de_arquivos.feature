# language: pt
Funcionalidade: Upload e anexacao de arquivos ao processo
  Para reunir num so lugar as pecas e os documentos recebidos do cliente
  Como advogado responsavel pelos autos
  Quero juntar arquivos ao processo, com o mesmo sigilo dos autos

  Cenario: arquivo juntado aos autos publicos fica disponivel para qualquer OAB
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"
    Quando eu junto aos autos o arquivo "procuracao.pdf" do tipo "application/pdf"
    Entao o anexo deve constar na lista de arquivos do processo
    E o anexo deve poder ser lido pela OAB "PE99999"
    E o advogado responsavel deve ser notificado sobre o arquivo juntado

  Cenario: arquivo juntado a processo sigiloso so e lido por OAB habilitada
    Dado um processo em segredo de justica "0007654-32.2026.8.17.0002" do cliente "M. R. S." na comarca de "Olinda"
    E o advogado responsavel "Bruno Carvalho" com e-mail "bruno@praxis.adv.br" e OAB "PE54321"
    Quando eu junto aos autos o arquivo "laudo.pdf" do tipo "application/pdf"
    Entao o anexo deve poder ser lido pela OAB "PE54321"
    Mas a leitura do anexo pela OAB "PE99999" deve ser negada
    E a leitura do anexo sem OAB deve ser negada

  Cenario: nome recebido do computador do usuario e higienizado
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"
    Quando eu junto aos autos o arquivo "../../etc/comprovante" do tipo "application/pdf"
    Entao o nome do anexo deve ser "comprovante.pdf"

  Cenario: tipo de arquivo fora da lista aceita e recusado
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"
    Quando eu tento juntar aos autos o arquivo "virus.exe" do tipo "application/x-msdownload"
    Entao a juntada deve ser recusada por tipo nao aceito

  Cenario: arquivo vazio e recusado
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"
    Quando eu tento juntar aos autos um arquivo vazio
    Entao a juntada deve ser recusada por falta de conteudo

  Cenario: nao se junta arquivo a processo inexistente
    Quando eu tento juntar o arquivo "peticao.pdf" ao processo "0009999-99.2026.8.17.0009"
    Entao a juntada deve ser recusada por processo inexistente
