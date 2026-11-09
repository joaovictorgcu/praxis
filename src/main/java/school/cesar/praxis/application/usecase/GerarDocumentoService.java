package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.application.port.out.ModeloRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.application.port.out.VersaoDocumentoRepositorio;
import school.cesar.praxis.domain.compartilhado.PoliticaDeLeituraDosAutos;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;
import school.cesar.praxis.domain.documento.DadosDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.GeradorDocumento;
import school.cesar.praxis.domain.documento.OrigemVersao;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.documento.VersaoDocumento;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.GeradorPorModelo;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Casos de uso: gerar peca a partir de template (Template Method no dominio) e
 * regenerar a peca com campos novos.
 *
 * <p>A escolha do gerador e feita por tipo ou por modelo cadastrado; o conteudo e
 * produzido pelo dominio; o documento resultante herda o segredo de justica do
 * processo e a lista de OABs habilitadas, que o Proxy vai conferir na leitura.
 * A peca guarda o modelo e os campos usados, e o texto gerado vira a versao 1
 * em {@code documento_versao} - tudo na mesma transacao.
 */
@Service
public class GerarDocumentoService implements DocumentosUseCases.GerarDocumento,
        DocumentosUseCases.RegenerarDocumento {

    private final ProcessoRepositorio processos;
    private final DocumentoRepositorio documentos;
    private final VersaoDocumentoRepositorio versoes;
    private final ModeloRepositorio modelos;
    private final Map<TipoDocumento, GeradorDocumento> geradores =
            new EnumMap<>(TipoDocumento.class);
    private final ObservadorProcesso observador;
    private final VerificadorDeAcessoAoProcesso verificadorAcesso;
    private final Relogio relogio;

    public GerarDocumentoService(ProcessoRepositorio processos,
                                 DocumentoRepositorio documentos,
                                 VersaoDocumentoRepositorio versoes,
                                 ModeloRepositorio modelos,
                                 List<GeradorDocumento> geradoresDisponiveis,
                                 ObservadorProcesso observador,
                                 VerificadorDeAcessoAoProcesso verificadorAcesso,
                                 Relogio relogio) {
        this.processos = processos;
        this.documentos = documentos;
        this.versoes = versoes;
        this.modelos = modelos;
        for (GeradorDocumento gerador : geradoresDisponiveis) {
            this.geradores.put(gerador.tipo(), gerador);
        }
        this.observador = observador;
        this.verificadorAcesso = verificadorAcesso;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.GerarDocumento.Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = carregarProcesso(numero);

        String codigoModelo = vazioComoNulo(comando.codigoModelo());
        GeradorDocumento gerador = escolherGerador(codigoModelo, comando.tipo());
        Map<String, String> campos = comando.campos() == null ? Map.of() : comando.campos();
        String conteudo = gerador.gerar(dadosDo(processo, campos));

        Set<String> habilitadas = new LinkedHashSet<>();
        habilitadas.add(processo.getResponsavel().oab());
        if (comando.oabSolicitante() != null && !comando.oabSolicitante().isBlank()) {
            habilitadas.add(comando.oabSolicitante());
        }

        // O tipo vem do gerador, e nao do comando: quem usa modelo cadastrado
        // produz a peca que o modelo declara.
        TipoDocumento tipo = gerador.tipo();
        DocumentoGerado documento = documentos.salvar(new DocumentoGerado(
                null,
                numero,
                tipo,
                conteudo,
                relogio.hoje(),
                comando.oabSolicitante() == null
                        ? processo.getResponsavel().oab()
                        : comando.oabSolicitante(),
                processo.isSegredoJustica(),
                habilitadas,
                codigoModelo == null ? null : CodigoModelo.de(codigoModelo).valor(),
                campos,
                1));

        versoes.registrar(new VersaoDocumento(null, documento.getId(), 1, documento.getConteudo(),
                OrigemVersao.GERACAO, documento.getGeradoPorOab(), comando.nomeSolicitante(), relogio.agora(),
                codigoModelo == null
                        ? "Gerada pela peça compilada (" + tipo.rotulo() + ")"
                        : "Gerada pelo modelo " + documento.getModeloCodigo()));

        observador.notificar(new EventoProcesso.DocumentoGerado(
                numero.valor(), processo.getResponsavel(), tipo.rotulo()));
        return documento;
    }

    /**
     * Regenera com o mesmo gerador da geracao. Os campos novos sao mesclados
     * sobre os guardados: quem corrige so a data nao precisa redigitar o resto.
     * O estado da peca (State) decide se o texto ainda pode mudar.
     */
    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.RegenerarDocumento.Comando comando) {
        DocumentoGerado documento = documentos.carregar(comando.documentoId());
        exigirAcesso(documento, comando.autorOab());
        Processo processo = carregarProcesso(documento.getNumeroProcesso());

        Map<String, String> campos = new LinkedHashMap<>(documento.getCamposGeracao());
        Set<String> alterados = new LinkedHashSet<>();
        if (comando.campos() != null) {
            comando.campos().forEach((chave, valor) -> {
                if (chave != null && !chave.isBlank() && valor != null && !valor.isBlank()
                        && !valor.trim().equals(campos.get(chave.trim()))) {
                    campos.put(chave.trim(), valor.trim());
                    alterados.add(chave.trim());
                }
            });
        }
        GeradorDocumento gerador = escolherGerador(documento.getModeloCodigo(), documento.getTipo());
        documento.regenerar(gerador.gerar(dadosDo(processo, campos)), campos);
        DocumentoGerado salvo = documentos.salvar(documento);

        versoes.registrar(new VersaoDocumento(null, salvo.getId(), salvo.getVersaoAtual(), salvo.getConteudo(),
                OrigemVersao.REGENERACAO, comando.autorOab(), comando.autorNome(), relogio.agora(),
                resumoDaRegeneracao(documento.getModeloCodigo(), alterados)));
        return salvo;
    }

    // --- Apoio ---

    private Processo carregarProcesso(NumeroCnj numero) {
        return processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo não encontrado: " + numero.valor()));
    }

    private static DadosDocumento dadosDo(Processo processo, Map<String, String> campos) {
        return new DadosDocumento(
                processo.getNumero().valor(),
                processo.getCliente(),
                processo.getComarca(),
                processo.getResponsavel(),
                campos);
    }

    /**
     * Modelo cadastrado tem precedencia; sem modelo, vale a peca compilada.
     * Os dois caminhos devolvem um {@code GeradorDocumento}, entao o resto do
     * caso de uso nao sabe qual foi escolhido.
     */
    private GeradorDocumento escolherGerador(String codigoModelo, TipoDocumento tipo) {
        if (codigoModelo != null) {
            CodigoModelo codigo = CodigoModelo.de(codigoModelo);
            return new GeradorPorModelo(modelos.porCodigo(codigo)
                    .orElseThrow(() -> new NoSuchElementException(
                            "modelo não encontrado: " + codigo)));
        }
        GeradorDocumento compilado = geradores.get(tipo);
        if (compilado == null) {
            throw new IllegalArgumentException(
                    "a peça " + tipo + " não tem gerador compilado;"
                            + " informe o código de um modelo cadastrado");
        }
        return compilado;
    }

    private void exigirAcesso(DocumentoGerado documento, String oab) {
        if (!PoliticaDeLeituraDosAutos.podeLer(documento, oab, verificadorAcesso, relogio.hoje())) {
            throw new ProxyDeAcesso.AcessoNegadoException("documento " + documento.getId()
                    + " está em segredo de justiça e a OAB " + oab + " não está habilitada nos autos");
        }
    }

    private static String resumoDaRegeneracao(String modelo, Set<String> alterados) {
        String origem = modelo == null ? "pela peça compilada" : "pelo modelo " + modelo;
        return alterados.isEmpty()
                ? "Regenerada " + origem + " com os mesmos campos"
                : "Regenerada " + origem + "; campos alterados: " + String.join(", ", alterados);
    }

    private static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
