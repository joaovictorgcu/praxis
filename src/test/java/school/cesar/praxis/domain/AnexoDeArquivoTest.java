package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.anexo.*;
import school.cesar.praxis.domain.compartilhado.AcessoRestrito;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade da anexacao de arquivos - dominio puro, sem Spring. */
class AnexoDeArquivoTest {

    private static final byte[] CONTEUDO = "conteudo do arquivo".getBytes();

    private ArquivoAnexo anexo(String nome, TipoArquivo tipo, boolean segredo) {
        return new ArquivoAnexo(1L, NumeroCnj.de("0001234-56.2026.8.17.0001"), nome, tipo,
                CONTEUDO, "Procuracao assinada", LocalDate.of(2026, 9, 9), "PE12345",
                segredo, Set.of("PE12345"));
    }

    // --- Nome do arquivo: vem de fora, entao nao e confiavel ---

    @Test
    @DisplayName("caminho de diretorio e descartado do nome recebido")
    void nomeDescartaCaminho() {
        assertEquals("procuracao.pdf",
                anexo("C:\\Users\\ana\\Documentos\\procuracao.pdf", TipoArquivo.PDF, false).getNome());
        assertEquals("laudo.pdf",
                anexo("../../etc/laudo.pdf", TipoArquivo.PDF, false).getNome());
    }

    @Test
    @DisplayName("extensao do tipo aceito e garantida no nome")
    void nomeGaranteExtensao() {
        assertEquals("comprovante.pdf", anexo("comprovante", TipoArquivo.PDF, false).getNome());
        assertEquals("foto.png", anexo("foto.png", TipoArquivo.PNG, false).getNome());
        // Nao duplica extensao ja correta, mesmo em maiuscula.
        assertEquals("LAUDO.PDF", anexo("LAUDO.PDF", TipoArquivo.PDF, false).getNome());
    }

    // --- Invariantes ---

    @Test
    @DisplayName("anexo exige processo, tipo, nome e conteudo")
    void invariantes() {
        NumeroCnj numero = NumeroCnj.de("0001234-56.2026.8.17.0001");

        assertThrows(IllegalArgumentException.class, () -> new ArquivoAnexo(null, null,
                "a.pdf", TipoArquivo.PDF, CONTEUDO, null, LocalDate.now(), "PE1", false, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new ArquivoAnexo(null, numero,
                "a.pdf", null, CONTEUDO, null, LocalDate.now(), "PE1", false, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new ArquivoAnexo(null, numero,
                " ", TipoArquivo.PDF, CONTEUDO, null, LocalDate.now(), "PE1", false, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new ArquivoAnexo(null, numero,
                "a.pdf", TipoArquivo.PDF, new byte[0], null, LocalDate.now(), "PE1", false, Set.of()));
    }

    @Test
    @DisplayName("arquivo acima do limite de 10 MB e recusado")
    void limiteDeTamanho() {
        byte[] grande = new byte[ArquivoAnexo.TAMANHO_MAXIMO_BYTES + 1];

        IllegalArgumentException falha = assertThrows(IllegalArgumentException.class,
                () -> new ArquivoAnexo(null, NumeroCnj.de("0001234-56.2026.8.17.0001"),
                        "grande.pdf", TipoArquivo.PDF, grande, null,
                        LocalDate.now(), "PE12345", false, Set.of()));

        assertTrue(falha.getMessage().contains("10 MB"), falha.getMessage());
    }

    @Test
    @DisplayName("tipo fora da lista aceita e recusado na porta")
    void tipoNaoAceito() {
        assertEquals(TipoArquivo.PDF, TipoArquivo.porMime("application/pdf"));
        assertEquals(TipoArquivo.JPEG, TipoArquivo.porMime("IMAGE/JPEG"));
        assertThrows(IllegalArgumentException.class,
                () -> TipoArquivo.porMime("application/x-msdownload"));
        assertThrows(IllegalArgumentException.class, () -> TipoArquivo.porMime(null));
    }

    @Test
    @DisplayName("conteudo e copiado na entrada e na saida, entao o agregado nao vaza estado")
    void conteudoImutavel() {
        byte[] original = "abc".getBytes();
        ArquivoAnexo anexo = new ArquivoAnexo(1L, NumeroCnj.de("0001234-56.2026.8.17.0001"),
                "a.txt", TipoArquivo.TEXTO, original, null, LocalDate.now(), "PE1", false, Set.of());

        original[0] = 'z';
        anexo.getConteudo()[1] = 'z';

        assertArrayEquals("abc".getBytes(), anexo.getConteudo());
    }

    // --- Proxy compartilhado com o documento gerado ---

    @Test
    @DisplayName("Proxy: anexo em segredo de justica barra OAB nao habilitada")
    void proxySegredoDeJustica() {
        ArquivoAnexo sigiloso = anexo("laudo.pdf", TipoArquivo.PDF, true);
        AcessoRestrito<ArquivoAnexo> real = id -> sigiloso;

        assertArrayEquals(CONTEUDO,
                new ArquivoProxy(real, "PE12345").carregar(1L).getConteudo());

        ProxyDeAcesso.AcessoNegadoException negado = assertThrows(
                ProxyDeAcesso.AcessoNegadoException.class,
                () -> new ArquivoProxy(real, "PE99999").carregar(1L));
        assertTrue(negado.getMessage().contains("arquivo 1"), negado.getMessage());

        assertThrows(ProxyDeAcesso.AcessoNegadoException.class,
                () -> new ArquivoProxy(real, "  ").carregar(1L));
    }

    @Test
    @DisplayName("Proxy: anexo publico e lido por qualquer OAB")
    void proxyAnexoPublico() {
        ArquivoAnexo publico = anexo("recibo.pdf", TipoArquivo.PDF, false);
        AcessoRestrito<ArquivoAnexo> real = id -> publico;

        assertEquals("recibo.pdf", new ArquivoProxy(real, "PE99999").carregar(1L).getNome());
    }
}
