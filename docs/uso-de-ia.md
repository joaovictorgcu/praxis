# Declaração de uso de IA

O slide 7 do enunciado dá duas opções: declaração de **não uso** de IA, ou entrega das **interações com IAs**. Este projeto usou IA, então segue a declaração e o registro.

## Ferramenta e escopo

- **Ferramenta:** Claude (Claude Code, modelo Opus 5), em sessão de terminal sobre o repositório.
- **Parte do trabalho coberta:** as duas funcionalidades de alta complexidade sob minha responsabilidade — **motor de prazos processuais com alertas** e **geração de documentos por template (petição inicial, contestação, procuração)** — além da estruturação em arquitetura limpa, do mapeamento objeto-relacional, da camada de apresentação web e destes documentos.

## Como a IA foi usada

| Etapa | Uso |
|---|---|
| Modelagem estratégica e tática | Discussão dos subdomínios, escolha de agregados (`Prazo` como agregado próprio referenciando `Processo` por identidade) e redação do `praxis.cml` |
| Implementação | Geração do código Java das camadas de domínio, aplicação, infraestrutura e apresentação, com revisão e ajuste manual |
| Padrões de projeto | Definição de onde cada padrão resolve problema real (Strategy na contagem legal, Template Method nas peças, Proxy no segredo de justiça, Observer/Decorator na notificação, Iterator na linha do tempo) |
| Testes | Escrita dos cenários Gherkin em português e dos step definitions Cucumber; testes de unidade do domínio |
| Documentação | Este arquivo, `dominio.md`, `mapa-historia-usuario.md` e o `README.md` |

## Verificação humana

Nada foi entregue sem execução e conferência:

- `./mvnw test` → **29 testes**, sendo **12 cenários BDD / 80 steps** do Cucumber, todos verdes.
- Contagem de prazo conferida manualmente contra o calendário de setembro/2026: intimação em 04/09 (sexta), feriado em 07/09 (segunda), prazo de 5 dias úteis → termo inicial 08/09 e vencimento em 14/09.
- Limite de 30% da quota litis conferido contra o art. 38 do Código de Ética da OAB.
- Regras de contagem conferidas contra os arts. 219, 220 e 224 do CPC; segredo de justiça contra o art. 189.

## Registro das interações

O histórico da sessão de desenvolvimento (prompts e respostas) está em `docs/interacoes-ia/`. Os prompts principais foram, em ordem:

1. Ler o enunciado do trabalho em grupo e implementar a minha parte: motor de prazos processuais com alertas (alta complexidade) e geração de documentos por template (alta complexidade).
2. Estruturar o projeto em arquitetura limpa, com DDD nos níveis preliminar, estratégico, tático e operacional.
3. Aplicar quatro ou mais dos padrões da lista do enunciado, cada um resolvendo um problema real do domínio.
4. Implementar a camada de persistência com mapeamento objeto-relacional e a camada de apresentação web.
5. Escrever os cenários BDD e automatizá-los com Cucumber.
6. Produzir a descrição do domínio com linguagem onipresente, o mapa da história do usuário, os protótipos de alta fidelidade e o modelo em Context Mapper (CML).
