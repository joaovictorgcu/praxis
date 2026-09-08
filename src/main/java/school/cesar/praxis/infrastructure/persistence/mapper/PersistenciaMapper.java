package school.cesar.praxis.infrastructure.persistence.mapper;

import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.prazo.NivelAlerta;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.processo.*;
import school.cesar.praxis.infrastructure.persistence.entity.AndamentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ProcessoEntity;

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

        return new DocumentoGerado(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getTipo(),
                entidade.getConteudo(),
                entidade.getGeradoEm(),
                entidade.getGeradoPorOab(),
                entidade.isSegredoJustica(),
                oabs);
    }
}
