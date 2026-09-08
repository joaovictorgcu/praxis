package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.documento.*;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.processo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PadroesDominioTest {

    @Test
    @DisplayName("Strategy: honorarios por modalidade")
    void honorarios() {
        assertEquals(new BigDecimal("5000"),
                new HonorarioFixo().calcular(BaseCalculo.fixo(new BigDecimal("5000"))));

        assertEquals(new BigDecimal("4500"),
                new HonorarioPorHora().calcular(BaseCalculo.porHora(new BigDecimal("450"), 10)));

        assertEquals(new BigDecimal("20000.00"),
                new HonorarioQuotaLitis().calcular(
                        BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("20"))));
    }

    @Test
    @DisplayName("Strategy: quota litis acima de 30 por cento viola limite etico")
    void quotaLitisLimite() {
        BaseCalculo base = BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("40"));
        assertThrows(IllegalArgumentException.class, () -> new HonorarioQuotaLitis().calcular(base));
    }

    @Test
    @DisplayName("Template Method: pecas compartilham esqueleto e divergem no corpo")
    void templateMethod() {
        DadosDocumento dados = new DadosDocumento("0001234-56.2026.8.17.0001", "Cliente Alfa",
                "Recife", Map.of("fatos", "fato relevante", "advogado", "Dra. Ana"));

        String inicial = new PeticaoInicial().gerar(dados);
        String procuracao = new Procuracao().gerar(dados);

        assertTrue(inicial.startsWith("PROCESSO N. 0001234-56.2026.8.17.0001"));
        assertTrue(inicial.contains("EXCELENTISSIMO"));
        assertTrue(inicial.contains("DOS FATOS"));
        assertTrue(inicial.contains("Termos em que pede deferimento."));

        // Hook sobrescrito: procuracao nao se enderaca ao juizo
        assertFalse(procuracao.contains("EXCELENTISSIMO"));
        assertTrue(procuracao.contains("PROCURACAO AD JUDICIA"));
        assertTrue(procuracao.contains("Dra. Ana"));
    }

    @Test
    @DisplayName("Proxy: segredo de justica bloqueia OAB nao habilitada")
    void proxySegredoJustica() {
        AcessoDocumento real = id -> "conteudo integral de " + id;

        AcessoDocumento habilitado = new DocumentoProxy(real, true, Set.of("PE12345"), "PE12345");
        assertEquals("conteudo integral de doc-1", habilitado.carregar("doc-1"));

        AcessoDocumento terceiro = new DocumentoProxy(real, true, Set.of("PE12345"), "PE99999");
        assertThrows(DocumentoProxy.AcessoNegadoException.class, () -> terceiro.carregar("doc-1"));

        AcessoDocumento publico = new DocumentoProxy(real, false, Set.of("PE12345"), "PE99999");
        assertEquals("conteudo integral de doc-1", publico.carregar("doc-1"));
    }

    @Test
    @DisplayName("Iterator: linha do tempo sai em ordem cronologica")
    void iteratorLinhaDoTempo() {
        Processo processo = new Processo(new NumeroCnj("0001234-56.2026.8.17.0003"), "Cliente Gama", false);
        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 8, 20), "Sentenca", TipoAndamento.SENTENCA));
        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 7, 1), "Citacao", TipoAndamento.CITACAO));
        processo.registrarAndamento(new Andamento(LocalDate.of(2026, 8, 1), "Audiencia", TipoAndamento.AUDIENCIA));

        List<String> descricoes = new java.util.ArrayList<>();
        for (Andamento andamento : processo) {
            descricoes.add(andamento.getDescricao());
        }

        assertEquals(List.of("Citacao", "Audiencia", "Sentenca"), descricoes);
    }

    @Test
    @DisplayName("Value Object: numero CNJ invalido e rejeitado")
    void numeroCnjInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new NumeroCnj("123"));
        assertDoesNotThrow(() -> new NumeroCnj("0001234-56.2026.8.17.0001"));
    }
}
