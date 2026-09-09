package school.cesar.praxis.infrastructure.persistence.mapper;

import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.RegistroAprovacao;
import school.cesar.praxis.domain.documento.StatusDocumento;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.DataUnica;
import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.RecorrenciaAnualFixa;
import school.cesar.praxis.domain.feriado.RegraRecorrencia;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;
import school.cesar.praxis.domain.modelo.TextoModelo;
import school.cesar.praxis.domain.prazo.NivelAlerta;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.processo.*;
import school.cesar.praxis.infrastructure.persistence.entity.AndamentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ArquivoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.FeriadoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ModeloEntity;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ProcessoEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tradutor entre modelo de dominio (puro) e entidades JPA. Existe justamente
 * para que o dominio nao carregue anotacao de framework - requisito da
 * arquitetura limpa.
 */
public final class PersistenciaMapper {

    private PersistenciaMapper() {
    }

    // --- Processo ---

    public static ProcessoEntity paraEntidade(Processo processo) {
        ProcessoEntity entidade = new ProcessoEntity();
        entidade.setId(processo.getId());
        entidade.setNumeroCnj(processo.getNumero().valor());
        entidade.setCliente(processo.getCliente());
        entidade.setComarca(processo.getComarca());
        entidade.setSegredoJustica(processo.isSegredoJustica());
        entidade.setResponsavelNome(processo.getResponsavel().nome());
        entidade.setResponsavelEmail(processo.getResponsavel().email());
        entidade.setResponsavelOab(processo.getResponsavel().oab());

        List<AndamentoEntity> andamentos = new ArrayList<>();
        for (Andamento andamento : processo) {
            AndamentoEntity item = new AndamentoEntity();
            item.setId(andamento.getId());
            item.setData(andamento.getData());
            item.setDescricao(andamento.getDescricao());
            item.setTipo(andamento.getTipo());
            andamentos.add(item);
        }
        entidade.setAndamentos(andamentos);
        return entidade;
    }

    public static Processo paraDominio(ProcessoEntity entidade) {
        Processo processo = new Processo(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroCnj()),
                entidade.getCliente(),
                entidade.getComarca(),
                entidade.isSegredoJustica(),
                new Advogado(entidade.getResponsavelNome(),
                        entidade.getResponsavelEmail(),
                        entidade.getResponsavelOab()));

        for (AndamentoEntity item : entidade.getAndamentos()) {
            processo.restaurarAndamento(new Andamento(
                    item.getId(), item.getData(), item.getDescricao(), item.getTipo()));
        }
        return processo;
    }

    // --- Prazo ---

    public static PrazoEntity paraEntidade(Prazo prazo) {
        PrazoEntity entidade = new PrazoEntity();
        entidade.setId(prazo.getId());
        entidade.setNumeroProcesso(prazo.getNumeroProcesso().valor());
        entidade.setDescricao(prazo.getDescricao());
        entidade.setIntimacao(prazo.getIntimacao());
        entidade.setQuantidadeDias(prazo.getQuantidadeDias());
        entidade.setRegime(prazo.getRegime());
        entidade.setVencimento(prazo.getVencimento());
        entidade.setFatal(prazo.isFatal());
        entidade.setResponsavelNome(prazo.getResponsavel().nome());
        entidade.setResponsavelEmail(prazo.getResponsavel().email());
        entidade.setResponsavelOab(prazo.getResponsavel().oab());
        entidade.setAlertasEmitidos(prazo.getAlertasEmitidos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(",")));
        entidade.setCumprido(prazo.isCumprido());
        entidade.setCumpridoEm(prazo.getCumpridoEm());
        return entidade;
    }

    public static Prazo paraDominio(PrazoEntity entidade) {
        Set<NivelAlerta> alertas = EnumSet.noneOf(NivelAlerta.class);
        if (entidade.getAlertasEmitidos() != null && !entidade.getAlertasEmitidos().isBlank()) {
            Arrays.stream(entidade.getAlertasEmitidos().split(","))
                    .map(String::trim)
                    .filter(valor -> !valor.isEmpty())
                    .map(NivelAlerta::valueOf)
                    .forEach(alertas::add);
        }

        return new Prazo(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getDescricao(),
                entidade.getIntimacao(),
                entidade.getQuantidadeDias(),
                entidade.getRegime(),
                entidade.getVencimento(),
                entidade.isFatal(),
                new Advogado(entidade.getResponsavelNome(),
                        entidade.getResponsavelEmail(),
                        entidade.getResponsavelOab()),
                alertas,
                entidade.isCumprido(),
                entidade.getCumpridoEm());
    }

    // --- Documento ---

    public static DocumentoEntity paraEntidade(DocumentoGerado documento) {
        DocumentoEntity entidade = new DocumentoEntity();
        entidade.setId(documento.getId());
        entidade.setNumeroProcesso(documento.getNumeroProcesso().valor());
        entidade.setTipo(documento.getTipo());
        entidade.setConteudo(documento.getConteudo());
        entidade.setGeradoEm(documento.getGeradoEm());
        entidade.setGeradoPorOab(documento.getGeradoPorOab());
        entidade.setSegredoJustica(documento.isSegredoJustica());
        entidade.setOabsHabilitadas(String.join(",", documento.getOabsHabilitadas()));
        entidade.setStatus(documento.getStatus().nome());
        entidade.setHistorico(serializarHistorico(documento.getHistorico()));
        return entidade;
    }

    public static DocumentoGerado paraDominio(DocumentoEntity entidade) {
        Set<String> oabs = new LinkedHashSet<>();
        if (entidade.getOabsHabilitadas() != null && !entidade.getOabsHabilitadas().isBlank()) {
            Arrays.stream(entidade.getOabsHabilitadas().split(","))
                    .map(String::trim)
                    .filter(valor -> !valor.isEmpty())
                    .forEach(oabs::add);
        }

        DocumentoGerado documento = new DocumentoGerado(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getTipo(),
                entidade.getConteudo(),
                entidade.getGeradoEm(),
                entidade.getGeradoPorOab(),
                entidade.isSegredoJustica(),
                oabs);
        documento.restaurarStatusPersistido(StatusDocumento.porNome(entidade.getStatus()));
        documento.restaurarHistoricoPersistido(desserializarHistorico(entidade.getHistorico()));
        return documento;
    }

    private static String serializarHistorico(List<RegistroAprovacao> historico) {
        return historico.stream()
                .map(r -> String.join("|",
                        r.getDeEstado(),
                        r.getParaEstado(),
                        r.getResponsavelOab() == null ? "" : r.getResponsavelOab(),
                        r.getComentario() == null ? "" : r.getComentario(),
                        r.getQuando().toString()))
                .collect(Collectors.joining(";"));
    }

    private static List<RegistroAprovacao> desserializarHistorico(String valor) {
        List<RegistroAprovacao> historico = new ArrayList<>();
        if (valor == null || valor.isBlank()) {
            return historico;
        }
        for (String entrada : valor.split(";")) {
            String[] partes = entrada.split("\\|", -1);
            historico.add(new RegistroAprovacao(
                    partes[0],
                    partes[1],
                    partes[2].isEmpty() ? null : partes[2],
                    partes[3].isEmpty() ? null : partes[3],
                    LocalDateTime.parse(partes[4])));
        }
        return historico;
    }

    // --- Feriado ---

    public static FeriadoEntity paraEntidade(Feriado feriado) {
        FeriadoEntity entidade = new FeriadoEntity();
        entidade.setId(feriado.getId());
        entidade.setDescricao(feriado.getDescricao());
        entidade.setData(feriado.getRecorrencia().dataDeReferencia());
        entidade.setRepeteTodoAno(feriado.getRecorrencia() instanceof RecorrenciaAnualFixa);
        entidade.setAbrangenciaNivel(feriado.getAbrangencia().nivel());
        entidade.setAbrangenciaValor(feriado.getAbrangencia().valor());
        return entidade;
    }

    public static Feriado paraDominio(FeriadoEntity entidade) {
        // A flag no banco e o que decide qual Strategy reconstruir.
        RegraRecorrencia recorrencia = entidade.isRepeteTodoAno()
                ? RecorrenciaAnualFixa.de(entidade.getData())
                : new DataUnica(entidade.getData());

        return new Feriado(
                entidade.getId(),
                entidade.getDescricao(),
                recorrencia,
                new Abrangencia(entidade.getAbrangenciaNivel(), entidade.getAbrangenciaValor()));
    }

    // --- Arquivo anexado ---

    public static ArquivoEntity paraEntidade(ArquivoAnexo anexo) {
        ArquivoEntity entidade = new ArquivoEntity();
        entidade.setId(anexo.getId());
        entidade.setNumeroProcesso(anexo.getNumeroProcesso().valor());
        entidade.setNome(anexo.getNome());
        entidade.setTipo(anexo.getTipo());
        entidade.setConteudo(anexo.getConteudo());
        entidade.setDescricao(anexo.getDescricao());
        entidade.setAnexadoEm(anexo.getAnexadoEm());
        entidade.setAnexadoPorOab(anexo.getAnexadoPorOab());
        entidade.setSegredoJustica(anexo.isSegredoJustica());
        entidade.setOabsHabilitadas(String.join(",", anexo.getOabsHabilitadas()));
        return entidade;
    }

    public static ArquivoAnexo paraDominio(ArquivoEntity entidade) {
        return new ArquivoAnexo(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getNome(),
                entidade.getTipo(),
                entidade.getConteudo(),
                entidade.getDescricao(),
                entidade.getAnexadoEm(),
                entidade.getAnexadoPorOab(),
                entidade.isSegredoJustica(),
                lerOabs(entidade.getOabsHabilitadas()));
    }

    /** OABs habilitadas ficam em coluna unica, separadas por virgula. */
    private static Set<String> lerOabs(String valor) {
        Set<String> oabs = new LinkedHashSet<>();
        if (valor != null && !valor.isBlank()) {
            Arrays.stream(valor.split(","))
                    .map(String::trim)
                    .filter(oab -> !oab.isEmpty())
                    .forEach(oabs::add);
        }
        return oabs;
    }

    // --- Modelo de documento ---

    public static ModeloEntity paraEntidade(ModeloDocumento modelo) {
        ModeloEntity entidade = new ModeloEntity();
        entidade.setId(modelo.getId());
        entidade.setCodigo(modelo.getCodigo().valor());
        entidade.setNome(modelo.getNome());
        entidade.setTipo(modelo.getTipo());
        entidade.setTitulo(modelo.getTitulo());
        entidade.setCorpo(modelo.getCorpo().texto());
        entidade.setPedidos(modelo.getPedidos().texto());
        entidade.setEnderecaAoJuizo(modelo.isEnderecaAoJuizo());
        return entidade;
    }

    public static ModeloDocumento paraDominio(ModeloEntity entidade) {
        return new ModeloDocumento(
                entidade.getId(),
                CodigoModelo.de(entidade.getCodigo()),
                entidade.getNome(),
                entidade.getTipo(),
                entidade.getTitulo(),
                new TextoModelo(entidade.getCorpo()),
                new TextoModelo(entidade.getPedidos()),
                entidade.isEnderecaAoJuizo());
    }
}