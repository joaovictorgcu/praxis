package school.cesar.praxis;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Migracao de dados, sem Spring: aplica as migracoes ate a V6, grava linhas no
 * formato antigo e aplica a V7 e a V8 por cima. Prova que quem ja tinha prazos
 * e pecas em producao nao perde nada: cada prazo ganha situacao, vencimento
 * original e o evento de abertura; cada peca ganha a versao 1 com o texto atual.
 *
 * <p>Tudo passa por uma conexao so: o H2 em memoria nao reavalia as restricoes
 * CHECK criadas por uma sessao que ja foi fechada.
 */
class MigracaoDeDadosV7V8Test {

    private static final String URL = "jdbc:h2:mem:praxis-migracao-dados;MODE=PostgreSQL;"
            + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

    @Test
    @DisplayName("V7 e V8 sobre dados da V6: situacao, trilha, versao 1 e restricoes de coerencia")
    void dadosAntigosSobrevivemAsMigracoes() throws SQLException {
        SingleConnectionDataSource banco = new SingleConnectionDataSource(URL, "sa", "", true);
        try {
            migrarEVerificar(banco);
        } finally {
            banco.destroy();
        }
    }

    private void migrarEVerificar(SingleConnectionDataSource banco) throws SQLException {
        Flyway.configure().dataSource(banco).target("6").load().migrate();
        try (Statement sql = banco.getConnection().createStatement()) {
            sql.executeUpdate("insert into prazo (numero_processo, descricao, intimacao, quantidade_dias, regime,"
                    + " vencimento, fatal, responsavel_nome, responsavel_email, responsavel_oab, alertas_emitidos,"
                    + " cumprido, cumprido_em) values ('0001234-56.2026.8.17.0001', 'Contestação', '2026-09-04', 5,"
                    + " 'DIAS_UTEIS', '2026-09-14', true, 'Ana Souza', 'ana@praxis.adv.br', 'PE12345', 'URGENTE',"
                    + " false, null)");
            sql.executeUpdate("insert into prazo (numero_processo, descricao, intimacao, quantidade_dias, regime,"
                    + " vencimento, fatal, responsavel_nome, responsavel_email, responsavel_oab, alertas_emitidos,"
                    + " cumprido, cumprido_em) values ('0001234-56.2026.8.17.0001', 'Juntada de procuração',"
                    + " '2026-09-01', 5, 'DIAS_UTEIS', '2026-09-09', false, 'Ana Souza', 'ana@praxis.adv.br',"
                    + " 'PE12345', null, true, '2026-09-03')");
            sql.executeUpdate("insert into documento (numero_processo, tipo, conteudo, gerado_em, gerado_por_oab,"
                    + " segredo_justica, oabs_habilitadas, status, historico) values ('0001234-56.2026.8.17.0001',"
                    + " 'PETICAO_INICIAL', 'Texto da peça antiga', '2026-09-01', 'PE12345', false, 'PE12345',"
                    + " 'RASCUNHO', null)");
        }

        Flyway.configure().dataSource(banco).load().migrate();

        try (Statement sql = banco.getConnection().createStatement()) {
            try (ResultSet prazos = sql.executeQuery("select descricao, situacao, vencimento_original, vencimento,"
                    + " prerrogativa, dias_acrescidos from prazo order by id")) {
                assertTrue(prazos.next());
                assertEquals("EM_ABERTO", prazos.getString("situacao"));
                assertEquals(prazos.getDate("vencimento"), prazos.getDate("vencimento_original"));
                assertEquals("NENHUMA", prazos.getString("prerrogativa"));
                assertEquals(0, prazos.getInt("dias_acrescidos"));
                assertTrue(prazos.next());
                assertEquals("CUMPRIDO", prazos.getString("situacao"));
            }
            assertEquals(2, contar(sql, "select count(*) from prazo_evento where tipo = 'ABERTURA'"));
            assertEquals(1, contar(sql, "select count(*) from prazo_evento where tipo = 'CUMPRIMENTO'"));

            try (ResultSet versao = sql.executeQuery("select v.numero, v.origem, v.conteudo, d.versao_atual"
                    + " from documento_versao v join documento d on d.id = v.documento_id")) {
                assertTrue(versao.next());
                assertEquals(1, versao.getInt("numero"));
                assertEquals("GERACAO", versao.getString("origem"));
                assertEquals("Texto da peça antiga", versao.getString("conteudo"));
                assertEquals(1, versao.getInt("versao_atual"));
                assertFalse(versao.next());
            }

            // O banco recusa o que o dominio nunca grava: situacao incoerente com o booleano antigo...
            assertThrows(SQLException.class, () -> sql.executeUpdate(
                    "update prazo set situacao = 'CUMPRIDO' where cumprido = false"));
            // ...periodo de suspensao que termina antes de comecar...
            assertThrows(SQLException.class, () -> sql.executeUpdate(
                    "insert into prazo_suspensao (prazo_id, inicio, fim, motivo)"
                            + " select min(id), '2026-09-10', '2026-09-08', 'invertido' from prazo"));
            // ...e versao repetida da mesma peca.
            assertThrows(SQLException.class, () -> sql.executeUpdate(
                    "insert into documento_versao (documento_id, numero, conteudo, origem, criada_em)"
                            + " select id, 1, 'duplicada', 'EDICAO', current_timestamp from documento"));
        }
    }

    private static long contar(Statement sql, String consulta) throws SQLException {
        try (ResultSet resultado = sql.executeQuery(consulta)) {
            resultado.next();
            return resultado.getLong(1);
        }
    }
}
