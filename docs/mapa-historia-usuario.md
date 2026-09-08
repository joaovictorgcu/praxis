# Mapa da história do usuário — praxis

Ator principal: **advogado responsável pelos autos**. Ator secundário: **secretaria/estagiário** (lança andamento e prazo).

## Espinha dorsal (atividades, na ordem em que acontecem no escritório)

```
Cadastrar processo → Acompanhar tramitação → Controlar prazos → Produzir peças → Cobrar honorários
```

## Mapa

| Cadastrar processo | Acompanhar tramitação | **Controlar prazos** | **Produzir peças** | Cobrar honorários |
|---|---|---|---|---|
| **Release 1 — MVP (o que está implementado)** | | | | |
| Cadastrar processo com número CNJ, cliente, comarca e responsável | Registrar andamento (intimação, citação, audiência, despacho, sentença, juntada) | Abrir prazo a partir da intimação, com contagem em dias úteis ou corridos | Gerar petição inicial a partir de template | Calcular honorário fixo, por hora e quota litis |
| Marcar processo em segredo de justiça | Ver linha do tempo em ordem cronológica | Ver agenda de prazos ordenada por vencimento | Gerar contestação a partir de template | Barrar quota litis acima de 30% |
| | Ser avisado de novo andamento | Receber alerta escalonado (5 / 3 / 1 / 0 dias) | Gerar procuração ad judicia | |
| | | Não receber o mesmo alerta duas vezes | Bloquear leitura de peça sigilosa por OAB não habilitada | |
| | | Registrar cumprimento do prazo | Listar peças geradas por processo | |
| | | Rodar varredura automática todo dia útil às 7h | Baixar a peça em texto | |
| **Release 2 — próximos passos (fora do MVP)** | | | | |
| Importar processo do PJe | Capturar andamento automaticamente do tribunal | Sincronizar agenda com Google Calendar | Exportar peça em .docx e .pdf | Emitir cobrança e conciliar pagamento |
| Vincular múltiplos advogados aos autos | Anexar documento do tribunal | Escalonar alerta para o sócio se o responsável não confirmar | Assinar peça digitalmente (ICP-Brasil) | Relatório de honorários por cliente |

## Histórias das duas funcionalidades de alta complexidade (minha parte)

### Motor de prazos processuais com alertas

> **Como** advogado responsável, **quero** ser avisado com antecedência dos prazos que estão vencendo, **para** não sofrer preclusão.

Critérios de aceitação (automatizados em `src/test/resources/features/motor_de_prazos.feature`):

1. Prazo fatal de 5 dias úteis intimado em 04/09/2026 vence em 14/09/2026 — fim de semana e feriado de 07/09 não contam.
2. Faltando 3 dias úteis, o responsável é notificado com nível `URGENTE` e a mensagem informa os dias restantes.
3. Rodar a varredura duas vezes no mesmo dia não gera notificação repetida.
4. Conforme o prazo se aproxima, o alerta escalona (`URGENTE` → `CRITICO`).
5. Prazo cumprido sai da varredura.
6. Prazo comum não gera alerta na política padrão do escritório.
7. Prazo fatal vencido e em aberto gera aviso de perda.

### Geração de documentos por template

> **Como** advogado, **quero** gerar petição inicial, contestação e procuração a partir de template, **para** não redigir a estrutura da peça na mão a cada processo.

Critérios de aceitação (automatizados em `src/test/resources/features/geracao_de_documentos.feature`):

1. A petição inicial sai com endereçamento à comarca correta, fatos, direito, pedidos e assinatura com OAB.
2. A procuração não se endereça ao juízo e traz os poderes especiais informados.
3. A contestação traz preliminares e mérito.
4. A peça gerada fica registrada nos autos e o responsável é avisado.
5. Peça de processo em segredo de justiça só é lida por OAB habilitada nos autos.
