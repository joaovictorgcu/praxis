package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.documento.*;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.notificacao.*;
import school.cesar.praxis.domain.processo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade da geracao de documentos e dos demais padroes do dominio. */
class GeracaoDeDocumentosTest {

    private final Advogado ana = new Advogado("Ana Souza", "ana@praxis.adv.br", "PE12345");
    private final DadosDocumento dados = new DadosDocumento(
            "0001234-56.2026.8.17.0001", "Construtora Alfa Ltda.", "Recife", ana,
            Map.of("fatos", "inadimplemento contratual",
                    "fundamentos", "art. 475 do Codigo Civil",
                    "poderesEspeciais", "receber citacao"));

    @Test
    @DisplayName("Template Method: as pecas compartilham o esqueleto e divergem no corpo")
    void esqueletoCompartilhado() {
        String inicial = new PeticaoInicial().gerar(dados);
        String contestacao = new Contestacao().gerar(dados);

        for (String peca : List.of(inicial, contestacao)) {
            assertTrue(peca.contains("PROCESSO N. 0001234-56.2026.8.17.0001"));
            assertTrue(peca.contains("EXCELENTISSIMO"));
            assertTrue(peca.contains("Construtora Alfa Ltda."));
            assertTrue(peca.contains("DOS PEDIDOS"));
            assertTrue(peca.contains("OAB PE12345"));
        }

        assertTrue(inicial.contains("DOS FATOS"));
        assertTrue(inicial.contains("inadimplemento contratual"));
        assertTrue(contestacao.contains("DAS PRELIMINARES"));
        assertFalse(contestacao.contains("DOS FATOS"));
    }

    @Test
    @DisplayName("Template Method: procuracao sobrescreve os hooks do juizo")
    void procuracaoSobrescreveHooks() {
        String procuracao = new Procuracao().gerar(dados);

        assertTrue(procuracao.startsWith("PROCURACAO AD JUDICIA ET EXTRA"));
        assertFalse(procuracao.contains("EXCELENTISSIMO"));
        assertTrue(procuracao.contains("OUTORGADO: Ana Souza"));
        assertTrue(procuracao.contains("receber citacao"));
        assertTrue(procuracao.contains("Outorgante"));
    }

    @Test
    @DisplayName("Proxy: segredo de justica barra OAB nao habilitada")
    void proxySegredoDeJustica() {
        DocumentoGerado sigiloso = new DocumentoGerado(1L,
                NumeroCnj.de("0007654-32.2026.8.17.0002"), TipoDocumento.PETICAO_INICIAL,
                "conteudo sigiloso", LocalDate.of(2026, 9, 8), "PE54321", true, Set.of("PE54321"));

        AcessoDocumento real = id -> sigiloso;

        assertEquals("conteudo sigiloso", new DocumentoProxy(real, "PE54321").carregar(1L).getConteudo());
        assertThrows(DocumentoProxy.AcessoNegadoException.class,
                () -> new DocumentoProxy(real, "PE99999").carregar(1L));
        assertThrows(DocumentoProxy.AcessoNegadoException.class,
                () -> new DocumentoProxy(real, "  ").carregar(1L));
    }

    @Test
    @DisplayName("Proxy: documento publico e lido por qualquer OAB")
    void documentoPublico() {
        DocumentoGerado publico = new DocumentoGerado(2L,
                NumeroCnj.de("0001234-56.2026.8.17.0001"), TipoDocumento.PROCURACAO,
                "conteudo publico", LocalDate.of(2026, 9, 8), "PE12345", false, Set.of("PE12345"));

        AcessoDocumento real = id -> publico;
        assertEquals("conteudo publico", new DocumentoProxy(real, "PE99999").carregar(2L).getConteudo());
        assertEquals("procuracao-0001234-56-2026-8-17-0001.txt", publico.nomeArquivo());
    }

    @Test
    @DisplayName("habilitar OAB da acesso a documento sigiloso que antes era negado")
    void habilitarOabDaAcesso() {
        DocumentoGerado sigiloso = new DocumentoGerado(3L,
                NumeroCnj.de("0007654-32.2026.8.17.0002"), TipoDocumento.PETICAO_INICIAL,
                "conteudo sigiloso", LocalDate.of(2026, 9, 8), "PE54321", true, Set.of("PE54321"));

        assertFalse(sigiloso.podeSerLidoPor("PE99999"));
        sigiloso.habilitarOab("PE99999");
        assertTrue(sigiloso.podeSerLidoPor("PE99999"));
    }

    @Test
    @DisplayName("revogar OAB tira o acesso que ela tinha")
    void revogarOabTiraAcesso() {
        DocumentoGerado sigiloso = new DocumentoGerado(4L,
                NumeroCnj.de("0007654-32.2026.8.17.0002"), TipoDocumento.PETICAO_INICIAL,
                "conteudo sigiloso", LocalDate.of(2026, 9, 8), "PE54321", true,
                new java.util.LinkedHashSet<>(Set.of("PE54321", "PE99999")));

        sigiloso.revogarOab("PE99999");

        assertFalse(sigiloso.podeSerLidoPor("PE99999"));
        assertTrue(sigiloso.podeSerLidoPor("PE54321"));
    }

    @Test
    @DisplayName("nao pode revogar a ultima OAB habilitada de documento sigiloso")
    void naoRevogaUltimaOabDeSigiloso() {
        DocumentoGerado sigiloso = new DocumentoGerado(5L,
                NumeroCnj.de("0007654-32.2026.8.17.0002"), TipoDocumento.PETICAO_INICIAL,
                "conteudo sigiloso", LocalDate.of(2026, 9, 8), "PE54321", true, Set.of("PE54321"));

        assertThrows(IllegalArgumentException.class, () -> sigiloso.revogarOab("PE54321"));
        assertTrue(sigiloso.podeSerLidoPor("PE54321"));
    }

    @Test
    @DisplayName("Decorator: painel, e-mail e auditoria recebem a mesma notificacao")
    void cadeiaDeDecorators() {
        List<Notificacao> painel = new ArrayList<>();
        List<Notificacao> auditoria = new ArrayList<>();

        Notificador base = painel::add;
        Notificador comAuditoria = new NotificadorDecorator(base) {
            @Override
            public void enviar(Notificacao notificacao) {
                super.enviar(notificacao);
                auditoria.add(notificacao);
            }
        };

        new AdvogadoResponsavel(comAuditoria).notificar(new EventoProcesso.DocumentoGerado(
                "0001234-56.2026.8.17.0001", ana, "Peticao inicial"));

        assertEquals(1, painel.size());
        assertEquals(1, auditoria.size());
        assertEquals("ana@praxis.adv.br", painel.get(0).destinatario());
    }

    @Test
    @DisplayName("Iterator: linha do tempo sai em ordem cronologica")
    void linhaDoTempoOrdenada() {
        Processo processo = new Processo(NumeroCnj.de("0001234-56.2026.8.17.0001"),
                "Construtora Alfa Ltda.", "Recife", false, ana);

        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 8, 20), "Sentenca", TipoAndamento.SENTENCA));
        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 7, 1), "Citacao", TipoAndamento.CITACAO));
        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 8, 1), "Audiencia", TipoAndamento.AUDIENCIA));

        List<String> descricoes = new ArrayList<>();
        for (Andamento andamento : processo) {
            descricoes.add(andamento.getDescricao());
        }

        assertEquals(List.of("Citacao", "Audiencia", "Sentenca"), descricoes);
        assertEquals("Citacao", processo.ultimaIntimacao().getDescricao());
    }

    @Test
    @DisplayName("Strategy: honorarios por modalidade, com limite etico da quota litis")
    void honorarios() {
        assertEquals(new BigDecimal("5000"),
                new HonorarioFixo().calcular(BaseCalculo.fixo(new BigDecimal("5000"))));
        assertEquals(new BigDecimal("4500"),
                new HonorarioPorHora().calcular(BaseCalculo.porHora(new BigDecimal("450"), 10)));
        assertEquals(new BigDecimal("20000.00"),
                new HonorarioQuotaLitis().calcular(
                        BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("20"))));

        BaseCalculo acima = BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("40"));
        assertThrows(IllegalArgumentException.class, () -> new HonorarioQuotaLitis().calcular(acima));
    }
}
