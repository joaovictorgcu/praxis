package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.documento.DadosDocumento;
import school.cesar.praxis.domain.documento.PeticaoInicial;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.modelo.*;
import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade do cadastro de modelos - dominio puro, sem Spring. */
class ModeloDeDocumentoTest {

    private final Advogado ana = new Advogado("Ana Souza", "ana@praxis.adv.br", "PE12345");
    private final DadosDocumento dados = new DadosDocumento(
            "0001234-56.2026.8.17.0001", "Construtora Alfa Ltda.", "Recife", ana,
            Map.of("valorDivida", "R$ 12.500,00"));

    private ModeloDocumento modelo(String corpo, String pedidos, boolean juizo, String titulo) {
        return new ModeloDocumento(
                CodigoModelo.de("COBRANCA_ALUGUEL"), "Cobranca de aluguel",
                TipoDocumento.PETICAO_INICIAL, titulo,
                new TextoModelo(corpo), new TextoModelo(pedidos), juizo);
    }

    // --- Interpreter ---

    @Test
    @DisplayName("Interpreter: marcador e trocado pelo valor do campo")
    void marcadorRecebeValor() {
        TextoModelo texto = new TextoModelo("Divida de {{valorDivida}} apurada.");

        assertEquals("Divida de R$ 12.500,00 apurada.",
                texto.interpretar(ContextoTexto.de(dados)));
    }

    @Test
    @DisplayName("Interpreter: campo nao informado vira marcador visivel, nao quebra a peca")
    void campoFaltandoViraMarcador() {
        TextoModelo texto = new TextoModelo("Imovel em {{enderecoImovel}}.");

        assertEquals("Imovel em (enderecoImovel a preencher).",
                texto.interpretar(ContextoTexto.de(dados)));
    }

    @Test
    @DisplayName("Interpreter: dados dos autos ficam disponiveis sem o usuario digitar")
    void nomesReservadosVemDosAutos() {
        TextoModelo texto = new TextoModelo(
                "{{cliente}} - {{comarca}} - {{processo}} - {{advogado}} - {{oab}}");

        assertEquals("Construtora Alfa Ltda. - Recife - 0001234-56.2026.8.17.0001"
                + " - Ana Souza - PE12345", texto.interpretar(ContextoTexto.de(dados)));
    }

    @Test
    @DisplayName("Interpreter: campo livre nao sobrescreve dado dos autos")
    void reservadoTemPrecedencia() {
        DadosDocumento comConflito = new DadosDocumento(
                "0001234-56.2026.8.17.0001", "Construtora Alfa Ltda.", "Recife", ana,
                Map.of("cliente", "Nome inventado"));

        assertEquals("Construtora Alfa Ltda.",
                new TextoModelo("{{cliente}}").interpretar(ContextoTexto.de(comConflito)));
    }

    @Test
    @DisplayName("Interpreter: texto sem marcador sai identico")
    void textoLiteralSaiIntacto() {
        assertEquals("DOS PEDIDOS\na) a procedencia.",
                new TextoModelo("DOS PEDIDOS\na) a procedencia.")
                        .interpretar(ContextoTexto.de(dados)));
    }

    @Test
    @DisplayName("placeholders sao extraidos na ordem, e camposEsperados exclui os reservados")
    void placeholdersECamposEsperados() {
        ModeloDocumento cobranca = modelo(
                "{{cliente}} deve {{valorDivida}} do imovel em {{enderecoImovel}}",
                "pagar {{valorDivida}} em {{comarca}}", true, null);

        assertEquals(List.of("cliente", "valorDivida", "enderecoImovel"),
                List.copyOf(cobranca.getCorpo().placeholders()));
        assertEquals(List.of("valorDivida", "enderecoImovel"),
                List.copyOf(cobranca.camposEsperados()));
    }

    // --- Coexistencia com o Template Method ---

    @Test
    @DisplayName("Template Method: peca por modelo mantem o mesmo esqueleto da compilada")
    void modeloMantemEsqueleto() {
        String porModelo = new GeradorPorModelo(
                modelo("DOS FATOS\nDivida de {{valorDivida}}.", "DOS PEDIDOS\na) pagar.", true, null))
                .gerar(dados);
        String compilada = new PeticaoInicial().gerar(dados);

        // As secoes de moldura sao as mesmas nos dois caminhos.
        for (String peca : List.of(porModelo, compilada)) {
            assertTrue(peca.contains("PROCESSO N. 0001234-56.2026.8.17.0001"));
            assertTrue(peca.contains("EXCELENTISSIMO SENHOR DOUTOR JUIZ DE DIREITO"
                    + " DA COMARCA DE RECIFE"));
            assertTrue(peca.contains("Construtora Alfa Ltda., por seu advogado"));
            assertTrue(peca.contains("Termos em que pede deferimento."));
            assertTrue(peca.contains("OAB PE12345"));
        }

        // E so o corpo e os pedidos divergem.
        assertTrue(porModelo.contains("Divida de R$ 12.500,00."));
        assertFalse(porModelo.contains("DO VALOR DA CAUSA"));
        assertTrue(compilada.contains("DO VALOR DA CAUSA"));
    }

    @Test
    @DisplayName("modelo que nao vai a juizo abandona toda a linguagem de juizo")
    void modeloSemEnderecamento() {
        String peca = new GeradorPorModelo(
                modelo("Acordo entre as partes.", "Quitacao reciproca.", false,
                        "Instrumento particular de acordo"))
                .gerar(dados);

        assertTrue(peca.startsWith("INSTRUMENTO PARTICULAR DE ACORDO - PROCESSO N."));
        assertTrue(peca.contains("Processo de referencia: 0001234-56.2026.8.17.0001"));
        assertTrue(peca.contains("firma o presente instrumento."));
        assertTrue(peca.contains("Recife, data da assinatura."));

        // Nenhuma das tres secoes do juizo pode vazar para uma peca extrajudicial.
        assertFalse(peca.contains("EXCELENTISSIMO"));
        assertFalse(peca.contains("Vossa Excelencia"));
        assertFalse(peca.contains("pede deferimento"));
    }

    @Test
    @DisplayName("o tipo da peca gerada vem do modelo, nao do pedido")
    void tipoVemDoModelo() {
        ModeloDocumento avulsa = new ModeloDocumento(
                CodigoModelo.de("ACORDO_EXTRAJUDICIAL"), "Acordo", TipoDocumento.PECA_AVULSA,
                null, new TextoModelo("corpo"), new TextoModelo("pedidos"), false);

        assertEquals(TipoDocumento.PECA_AVULSA, new GeradorPorModelo(avulsa).tipo());
    }

    // --- Invariantes ---

    @Test
    @DisplayName("codigo do modelo e autovalidado e normalizado")
    void invariantesDoCodigo() {
        assertEquals("COBRANCA_ALUGUEL", CodigoModelo.de(" cobranca_aluguel ").valor());
        assertThrows(IllegalArgumentException.class, () -> CodigoModelo.de("ab"));
        assertThrows(IllegalArgumentException.class, () -> CodigoModelo.de("com espaco"));
        assertThrows(IllegalArgumentException.class, () -> CodigoModelo.de(null));
    }

    @Test
    @DisplayName("modelo exige codigo, nome, tipo, corpo e pedidos")
    void invariantesDoModelo() {
        TextoModelo texto = new TextoModelo("x");
        CodigoModelo codigo = CodigoModelo.de("MODELO_X");

        assertThrows(IllegalArgumentException.class, () -> new TextoModelo(" "));
        assertThrows(IllegalArgumentException.class, () -> new ModeloDocumento(
                codigo, " ", TipoDocumento.PECA_AVULSA, null, texto, texto, true));
        assertThrows(IllegalArgumentException.class, () -> new ModeloDocumento(
                codigo, "Nome", null, null, texto, texto, true));
        assertThrows(IllegalArgumentException.class, () -> new ModeloDocumento(
                codigo, "Nome", TipoDocumento.PECA_AVULSA, null, null, texto, true));
        assertThrows(IllegalArgumentException.class, () -> new GeradorPorModelo(null));
    }
}
