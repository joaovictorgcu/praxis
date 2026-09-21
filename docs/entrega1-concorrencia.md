# Entrega 1 — Arquitetura, protótipo single-node com concorrência local

## 1. Escopo e contexto

Esta entrega usa a funcionalidade de Agenda de Audiências do Praxis como cenário de concorrência local. O objetivo não é mudar a arquitetura do projeto nem introduzir microserviços: é demonstrar, dentro da mesma JVM, como a validação de conflito e o cadastro de uma audiência podem correr em paralelo e produzir uma condição de corrida.

O sistema já possui a regra de negócio principal na camada de domínio: uma audiência não pode sobrepor outra na mesma sala e no mesmo intervalo de horário. A entrega 1 usa esse comportamento como base de diagnóstico e de correção.

## 2. Arquitetura atual do Praxis e o fluxo relevante

A arquitetura do projeto continua sendo:

- Presentation: controllers REST e web MVC
- Application: casos de uso e orquestração
- Domain: agregados, regras e validações
- Infrastructure: persistência, mapeamento JPA, segurança e integrações

Fluxo do caso de uso de audiência:

1. Controller recebe a requisição HTTP
2. AppService valida horário e constrói a entidade de domínio
3. O agregado AgendaDeAudiencias verifica conflito
4. Repositório persiste a entidade no banco
5. A resposta retorna ao cliente

Em relação ao cenário de concorrência, o recurso compartilhado é a agenda da sala: todas as threads acessam a mesma lista de audiências da mesma sala e tentam validar a mesma regra ao mesmo tempo.

## 3. Recurso compartilhado e condição de corrida

O problema é o seguinte:

- Thread A lê a agenda atual
- Thread B lê a mesma agenda atual
- Ambas verificam que não existe conflito
- Ambas passam pela validação antes que qualquer uma confirme o cadastro
- Ambas persistecem audiências sobrepostas na mesma sala e no mesmo horário

Esse é o típico problema de check-then-act. A operação não é atômica: a verificação e o cadastro estão separados, então a regra de negócio pode ser violada em um único processo.

## 4. Seção crítica e sincronização single-node

A seção crítica é a validação e persistência do conflito para a mesma sala:

- verificar se existe conflito
- decidir se aceita ou rejeita
- inserir a audiência

Para uma JVM única, a estratégia correta é bloquear a região crítica com synchronized ou com um lock compartilhado, de modo que apenas uma thread execute a verificação+inserção por vez.

Essa abordagem resolve o problema dentro do mesmo processo, mas não resolve concorrência distribuída entre múltiplas instâncias do Praxis. Em um ambiente multi-node, cada JVM teria uma visão local da agenda e a sincronização single-node não seria suficiente.

## 5. Implementação escolhida

A parte funcional foi desenvolvida em um experimento dedicado, sem mexer na regra de negócio principal do sistema:

- baseline sem sincronização: demonstra a violação da regra
- versão sincronizada: garante que em uma JVM só uma tentativa seja aceita por vez

O experimento está em:

- src/main/java/school/cesar/praxis/concorrencia/AgendaConcorrenciaExperimento.java
- src/test/java/school/cesar/praxis/concorrencia/AgendaConcorrenciaExperimentoTest.java

## 6. Resultado do experimento

A regra esperada para N tentativas da mesma audiência conflitante é:

- 1 aceita
- N-1 rejeitadas

Com a execução real do experimento, a versão sem sincronização gera violação; a versão sincronizada mantém a regra. O teste de validação do experimento garante que:

- baseline apresenta violação
- sincronizada rejeita as tentativas extras
- 5 rodadas são executadas para evidenciar o comportamento

## 7. Limitações da solução

- Resolve o problema dentro de uma única JVM
- Não resolve concorrência distribuída entre múltiplas instâncias
- Não substitui o banco nem o modelo distribuído
- É uma base para evolução futura em um sistema tolerante a falhas e com coordenação global

## 8. IA e documentação

A IA foi usada como copiloto e não como autor da solução final. Os prompts foram usados para analisar: escolha do cenário, raciocínio da condição de corrida, desenho do protótipo e análise dos resultados.

### Prompts documentados

1. Identificação de uma funcionalidade adequada para demonstrar concorrência no Praxis.
2. Análise da condição de corrida na Agenda de Audiências.
3. Criação/análise do protótipo concorrente.
4. Análise dos resultados das 5 execuções.

## 9. Decisões aceitas e rejeitadas

Decisões aceitas:

- usar a Agenda de Audiências como cenário da entrega
- manter a arquitetura atual
- poupar regras de negócio existentes
- demonstrar o problema em uma única JVM
- documentar a limitação do single-node

Decisões rejeitadas:

- transformar o projeto em distribuído antes do momento
- usar Redis, Kafka e microserviços nesta entrega
- alterar a regra de conflito do domínio
- inventar resultados de execução

## 10. Como explicar ao professor

O professor deve ouvir a resposta central assim:

- o projeto já tinha a regra de conflito de audiência;
- a entrega 1 usou esse cenário para demonstrar um problema real de concorrência;
- a falha é de check-then-act, não de regra de negócio isolada;
- a solução correta, em single-node, é sincronizar a seção crítica;
- essa abordagem é válida para a JVM atual, mas não substitui sincronização distribuída em cenários reais multi-instância.
