package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.application.port.out.ArquivoRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.anexo.AcessoArquivo;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.anexo.ArquivoProxy;
import school.cesar.praxis.domain.anexo.TipoArquivo;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Casos de uso da juntada de arquivos. A juntada copia o segredo de justica e
 * as OABs habilitadas do processo, e a leitura nunca toca o repositorio direto:
 * vai pelo {@link ArquivoProxy}.
 */
@Service
public class AnexoAppService implements AnexosUseCases.AnexarArquivo,
        AnexosUseCases.ListarAnexos,
        AnexosUseCases.BaixarAnexo {

    private final ProcessoRepositorio processos;
    private final ArquivoRepositorio arquivos;
    private final ObservadorProcesso observador;
    private final Relogio relogio;

    public AnexoAppService(ProcessoRepositorio processos,
                           ArquivoRepositorio arquivos,
                           ObservadorProcesso observador,
                           Relogio relogio) {
        this.processos = processos;
        this.arquivos = arquivos;
        this.observador = observador;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public AnexosUseCases.ItemAnexo executar(Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo nao encontrado: " + comando.numeroProcesso()));

        Set<String> habilitadas = new LinkedHashSet<>();
        habilitadas.add(processo.getResponsavel().oab());
        if (comando.oabSolicitante() != null && !comando.oabSolicitante().isBlank()) {
            habilitadas.add(comando.oabSolicitante());
        }

        ArquivoAnexo salvo = arquivos.salvar(new ArquivoAnexo(
                null,
                numero,
                comando.nomeArquivo(),
                TipoArquivo.porMime(comando.tipoConteudo()),
                comando.conteudo(),
                comando.descricao(),
                relogio.hoje(),
                comando.oabSolicitante() == null
                        ? processo.getResponsavel().oab()
                        : comando.oabSolicitante(),
                processo.isSegredoJustica(),
                habilitadas));

        observador.notificar(new EventoProcesso.ArquivoAnexado(
                numero.valor(), processo.getResponsavel(), salvo.getNome()));

        return paraItem(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnexosUseCases.ItemAnexo> executar(String numeroProcesso) {
        List<ArquivoAnexo> encontrados = numeroProcesso == null || numeroProcesso.isBlank()
                ? arquivos.listar()
                : arquivos.porProcesso(NumeroCnj.de(numeroProcesso));

        return encontrados.stream().map(AnexoAppService::paraItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArquivoAnexo executar(Long anexoId, String oabSolicitante) {
        AcessoArquivo acessoProtegido = new ArquivoProxy(arquivos, oabSolicitante);
        return acessoProtegido.carregar(anexoId);
    }

    private static AnexosUseCases.ItemAnexo paraItem(ArquivoAnexo anexo) {
        return new AnexosUseCases.ItemAnexo(
                anexo.getId(),
                anexo.getNumeroProcesso().valor(),
                anexo.getNome(),
                anexo.getTipo(),
                anexo.getTipo().rotulo(),
                anexo.tamanhoBytes(),
                anexo.getDescricao(),
                anexo.getAnexadoEm(),
                anexo.getAnexadoPorOab(),
                anexo.isSegredoJustica());
    }
}
