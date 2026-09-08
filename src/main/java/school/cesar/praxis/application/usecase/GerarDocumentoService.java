package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.documento.DadosDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.GeradorDocumento;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Caso de uso: gerar peca a partir de template (Template Method no dominio).
 *
 * <p>A escolha do gerador e feita por tipo; o conteudo e produzido pelo dominio;
 * o documento resultante herda o segredo de justica do processo e a lista de OABs
 * habilitadas, que o Proxy vai conferir na leitura.
 */
@Service
public class GerarDocumentoService implements DocumentosUseCases.GerarDocumento {

    private final ProcessoRepositorio processos;
    private final DocumentoRepositorio documentos;
    private final Map<TipoDocumento, GeradorDocumento> geradores =
            new EnumMap<>(TipoDocumento.class);
    private final ObservadorProcesso observador;
    private final Relogio relogio;

    public GerarDocumentoService(ProcessoRepositorio processos,
                                 DocumentoRepositorio documentos,
                                 List<GeradorDocumento> geradoresDisponiveis,
                                 ObservadorProcesso observador,
                                 Relogio relogio) {
        this.processos = processos;
        this.documentos = documentos;
        for (GeradorDocumento gerador : geradoresDisponiveis) {
            this.geradores.put(gerador.tipo(), gerador);
        }
        this.observador = observador;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public DocumentoGerado executar(Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo nao encontrado: " + comando.numeroProcesso()));

        GeradorDocumento gerador = geradores.get(comando.tipo());
        if (gerador == null) {
            throw new IllegalArgumentException("sem template para a peca " + comando.tipo());
        }

        DadosDocumento dados = new DadosDocumento(
                numero.valor(),
                processo.getCliente(),
                processo.getComarca(),
                processo.getResponsavel(),
                comando.campos());

        String conteudo = gerador.gerar(dados);

        Set<String> habilitadas = new LinkedHashSet<>();
        habilitadas.add(processo.getResponsavel().oab());
        if (comando.oabSolicitante() != null && !comando.oabSolicitante().isBlank()) {
            habilitadas.add(comando.oabSolicitante());
        }

        DocumentoGerado documento = documentos.salvar(new DocumentoGerado(
                null,
                numero,
                comando.tipo(),
                conteudo,
                relogio.hoje(),
                comando.oabSolicitante() == null
                        ? processo.getResponsavel().oab()
                        : comando.oabSolicitante(),
                processo.isSegredoJustica(),
                habilitadas));

        observador.notificar(new EventoProcesso.DocumentoGerado(
                numero.valor(), processo.getResponsavel(), comando.tipo().rotulo()));

        return documento;
    }
}
