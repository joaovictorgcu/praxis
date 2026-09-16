# Implantacao

Publicar no Render (plano gratuito) e rodar no perfil prod com PostgreSQL e Flyway.

---

O repositório já traz o que a plataforma precisa: [`Dockerfile`](../Dockerfile) (compila e entrega só o JRE com o jar) e [`render.yaml`](../render.yaml), um blueprint que cria o banco e o serviço web juntos.

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/joaovictorgcu/praxis)

1. Clique no botão acima (ou, no painel do Render, **New → Blueprint**) e entre com a conta do GitHub.
2. Escolha o repositório `praxis`. O Render pede o valor de `PRAXIS_ADMIN_SENHA` (a única variável marcada `sync: false`): é a senha do administrador da instância, escolhida por quem publica e guardada só no painel — o blueprint não a traz escrita. Confirme em **Apply**.
3. O Render cria o PostgreSQL `praxis-db`, injeta host, porta, base, usuário e senha no serviço web, compila a imagem e publica em `https://<nome>.onrender.com`. O primeiro build leva de 5 a 8 minutos.

No primeiro boot, o Flyway aplica `V1__esquema_inicial.sql` e a carga de exemplo monta o escritório. A instância publicada é uma **demonstração**: entra com `admin@admin` e a senha definida no Apply, direto, sem troca de senha. Sem `PRAXIS_ADMIN_SENHA` no ambiente o administrador não é criado — nenhuma senha padrão fica valendo.

Para virar instalação real, troque as variáveis no painel do Render (Environment) e faça um redeploy:

```
PRAXIS_ADMIN_USUARIO=<seu e-mail>   PRAXIS_ADMIN_SENHA=<senha forte>
PRAXIS_DADOS_EXEMPLO=false          PRAXIS_EXIGIR_TROCA=true
PRAXIS_SENHA_INICIAL=<senha forte>
```

**O que o plano gratuito cobra em outra moeda:**

- O serviço dorme depois de 15 minutos sem tráfego; a primeira visita depois disso espera a JVM subir (~30 a 60 s). Como a sessão vive em memória, quem estava logado precisa entrar de novo.
- A varredura automática das 7h só roda se a instância estiver acordada. Um ping externo diário resolve, se isso importar.
- O PostgreSQL gratuito do Render **expira em 30 dias**. Depois disso, crie um banco gratuito permanente (Neon, Supabase) e troque as cinco variáveis de banco por uma só:
  `PRAXIS_DB_URL=jdbc:postgresql://<host>/<base>?sslmode=require`, mais `PRAXIS_DB_USER` e `PRAXIS_DB_PASSWORD`.
- 512 MB de RAM: a imagem já sobe com `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC`.
- Anexos moram em BLOB no banco, e o plano gratuito dá pouco espaço — é demonstração, não arquivo do escritório.

`ImplantacaoHttpTest` sobe a aplicação com o perfil `prod` (Flyway criando o esquema, Hibernate só validando) e confirma o que a instância publicada promete: `admin@admin` entra com a senha injetada pelo ambiente, não cai na troca de senha e encontra o escritório de exemplo montado.

## Rodar em produção: PostgreSQL + Flyway

```bash
SPRING_PROFILES_ACTIVE=prod PRAXIS_DB_URL=jdbc:postgresql://localhost:5432/praxis PRAXIS_DB_USER=praxis PRAXIS_DB_PASSWORD=segredo PRAXIS_SENHA_INICIAL=troque-ja ./mvnw spring-boot:run
```

- O esquema é versionado em [`src/main/resources/db/migration`](../src/main/resources/db/migration) (`V1__esquema_inicial.sql`); o Hibernate roda com `ddl-auto=validate`, então toda mudança de entidade exige uma nova `V{n}__*.sql`.
- Em dev/test o Flyway fica desligado e o H2 em memória segue com `ddl-auto=update`. `MigracaoFlywayTest` sobe a aplicação com H2 em modo PostgreSQL, aplica a migração e deixa o Hibernate validar — migração e entidades não divergem sem um teste quebrar.
- Binário e texto longo são `bytea`/`text` (colunas comuns), não large objects (`oid`): entram em backup e transação como qualquer coluna.
- No perfil prod os usuários iniciais nascem com **senha provisória** (`praxis.exigir-troca-senha-inicial=true`): o primeiro acesso cai na troca de senha; a carga de exemplo não roda; o cookie de sessão é `Secure`. O usuário `admin` só existe se `PRAXIS_ADMIN_SENHA` for definida.

---
