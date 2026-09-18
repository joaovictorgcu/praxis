# language: pt
Funcionalidade: Cálculo de Honorários

  Cenário: Calcular honorário fixo
    Dado que a modalidade de honorário é fixa com valor de 5000.00
    Quando o honorário for calculado
    Então o valor do honorário deve ser 5000.00
    E a data de vencimento deve ser fixada em "2026-12-31"

  Cenário: Calcular honorário por hora
    Dado que a modalidade é por hora com valor hora de 450.00 e 10 horas trabalhadas
    Quando o honorário for calculado
    Então o valor do honorário deve ser 4500.00
    E a data de vencimento deve ser fixada em "2026-12-31"

  Cenário: Calcular honorário quota litis dentro do limite ético
    Dado que a modalidade é quota litis com valor da causa de 100000.00 e percentual de 20 por cento
    Quando o honorário for calculado
    Então o valor do honorário deve ser 20000.00
    E a data de vencimento deve ser fixada em "2026-12-31"

  Cenário: Tentar calcular honorário quota litis acima do limite ético
    Dado que a modalidade é quota litis com valor da causa de 100000.00 e percentual de 40 por cento
    Quando o honorário for calculado
    Então deve lançar uma exceção de limite ético excedido

  Cenário: Registrar pagamento de honorário
    Dado que existe um honorário fixo de 5000.00 com status "PENDENTE"
    Quando o pagamento do honorário for confirmado
    Então o status do honorário deve ser alterado para "PAGO"
    E a data de liquidação deve ser fixada em "2026-12-31"

  Cenário: Consultar vencimento de honorário
    Dado que existe um honorário cadastrado
    Quando a data de vencimento for consultada
    Então a data retornada deve ser a data fixa "2026-12-31"