package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.documento.ComparadorDeVersoes;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.LinhaComparada;
import school.cesar.praxis.domain.documento.OrigemVersao;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.documento.VersaoDocumento;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade do texto versionado da peca e da comparacao entre versoes. */
class VersoesDaPecaTest {

    private static final String TEXTO = "DOS FATOS\nA decisão foi omissa.\nDOS PEDIDOS\na) suprir a omissão.";

    private static DocumentoGerado rascunho() {
        return new DocumentoGerado(7L, NumeroCnj.de("0013579-56.2026.8.17.0001"), TipoDocumento.PECA_AVULSA,
                TEXTO, LocalDate.of(2026, 11, 9), "PE45678", false, Set.of("PE45678"),
                "EMBARGOS_DECLARACAO", Map.of("omissao", "a tutela"), 1);
    }

    @Test
    @DisplayName("State: rascunho e peca rejeitada aceitam edicao; em revisao, aprovada e protocolada recusam")
    void edicaoPorEstado() {
        DocumentoGerado peca = rascunho();
        peca.editar(TEXTO + "\nb) intimar a parte embargada.");
        assertEquals(2, peca.getVersaoAtual());

        peca.enviarParaRevisao();
        IllegalStateException emRevisao = assertThrows(IllegalStateException.class, () -> peca.editar("outro"));
        assertTrue(emRevisao.getMessage().contains("editar"));

        peca.rejeitar("PE00001", "Indicar as folhas da decisão");
        peca.editar(TEXTO + "\nb) intimar a parte embargada (fls. 85).");
        assertEquals(3, peca.getVersaoAtual());
        assertEquals("REJEITADO", peca.getStatus().nome(), "editar nao muda o estado");

        peca.enviarParaRevisao();
        peca.aprovar("PE00001", "Pode protocolar");
        assertThrows(IllegalStateException.class, () -> peca.editar("depois de aprovada"));
        peca.protocolar();
        assertThrows(IllegalStateException.class, () -> peca.editar("depois de protocolada"));
        assertEquals(3, peca.getVersaoAtual());
    }

    @Test
    @DisplayName("edicao normaliza CRLF do navegador; texto igual nao vira versao nova")
    void edicaoSemMudanca() {
        DocumentoGerado peca = rascunho();
        IllegalArgumentException igual = assertThrows(IllegalArgumentException.class,
                () -> peca.editar(TEXTO.replace("\n", "\r\n")));
        assertTrue(igual.getMessage().contains("não mudou"));
        assertEquals(1, peca.getVersaoAtual());
        assertThrows(IllegalArgumentException.class, () -> peca.editar("   "));
    }

    @Test
    @DisplayName("regenerar troca texto e campos; restaurar traz o texto de versao anterior como versao nova")
    void regenerarERestaurar() {
        DocumentoGerado peca = rascunho();
        peca.regenerar(TEXTO.replace("omissa", "omissa quanto à tutela"), Map.of("omissao", "a tutela de urgência"));
        assertEquals(2, peca.getVersaoAtual());
        assertEquals("a tutela de urgência", peca.getCamposGeracao().get("omissao"));

        VersaoDocumento v1 = new VersaoDocumento(1L, 7L, 1, TEXTO, OrigemVersao.GERACAO, "PE45678", null,
                LocalDateTime.of(2026, 11, 9, 9, 0), null);
        peca.restaurar(v1);
        assertEquals(3, peca.getVersaoAtual());
        assertEquals(TEXTO, peca.getConteudo());

        VersaoDocumento deOutraPeca = new VersaoDocumento(9L, 8L, 1, "outro", OrigemVersao.GERACAO, null, null,
                LocalDateTime.of(2026, 11, 9, 9, 0), null);
        assertThrows(IllegalArgumentException.class, () -> peca.restaurar(deOutraPeca));
        VersaoDocumento futura = new VersaoDocumento(2L, 7L, 3, "x", OrigemVersao.EDICAO, null, null,
                LocalDateTime.of(2026, 11, 9, 9, 0), null);
        assertThrows(IllegalArgumentException.class, () -> peca.restaurar(futura));
        // v1 e o texto atual de novo: restaurar a v1 outra vez nao muda nada.
        assertThrows(IllegalArgumentException.class, () -> peca.restaurar(v1));
    }

    @Test
    @DisplayName("exclusao: so rascunho que nunca foi para revisao, por quem gerou ou pelo chefe")
    void exclusao() {
        DocumentoGerado peca = rascunho();
        assertDoesNotThrow(() -> peca.exigirExclusaoPermitida("PE45678", false));
        assertDoesNotThrow(() -> peca.exigirExclusaoPermitida("PE00001", true));
        assertThrows(IllegalStateException.class, () -> peca.exigirExclusaoPermitida("PE12345", false));

        peca.enviarParaRevisao();
        peca.rejeitar("PE00001", "refazer");
        IllegalStateException revisada = assertThrows(IllegalStateException.class,
                () -> peca.exigirExclusaoPermitida("PE45678", false));
        assertTrue(revisada.getMessage().contains("nunca foi para revisão"));
    }

    @Test
    @DisplayName("LCS: linha trocada sai como remocao seguida de inclusao; o resto fica igual")
    void comparacaoLinhaALinha() {
        String antes = "DOS FATOS\nA decisão de 30/10/2026 foi omissa.\nDOS PEDIDOS\na) suprir a omissão.";
        String depois = "DOS FATOS\nA decisão de 30/10/2026 (fls. 85) foi omissa.\nDOS PEDIDOS\na) suprir a omissão."
                + "\nb) efeito modificativo.";

        List<LinhaComparada> linhas = ComparadorDeVersoes.comparar(antes, depois);
        assertEquals(List.of(LinhaComparada.Tipo.IGUAL, LinhaComparada.Tipo.REMOVIDA, LinhaComparada.Tipo.INCLUIDA,
                        LinhaComparada.Tipo.IGUAL, LinhaComparada.Tipo.IGUAL, LinhaComparada.Tipo.INCLUIDA),
                linhas.stream().map(LinhaComparada::tipo).toList());
        assertEquals(2, linhas.get(1).numeroAntes());
        assertNull(linhas.get(1).numeroDepois());
        assertEquals(5, linhas.get(5).numeroDepois());
        assertEquals("b) efeito modificativo.", linhas.get(5).texto());
    }

    @Test
    @DisplayName("LCS: textos iguais so tem linhas iguais; texto vazio vira so inclusoes")
    void comparacaoNosExtremos() {
        assertTrue(ComparadorDeVersoes.comparar(TEXTO, TEXTO).stream().noneMatch(LinhaComparada::alterada));
        List<LinhaComparada> doZero = ComparadorDeVersoes.comparar("", "a\nb");
        assertEquals(2, doZero.size());
        assertTrue(doZero.stream().allMatch(l -> l.tipo() == LinhaComparada.Tipo.INCLUIDA));
        // CRLF e LF sao a mesma quebra de linha para a comparacao.
        assertTrue(ComparadorDeVersoes.comparar("a\r\nb", "a\nb").stream().noneMatch(LinhaComparada::alterada));
    }
}
