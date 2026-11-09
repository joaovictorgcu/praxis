package school.cesar.praxis.infrastructure.persistence.mapper;

import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.acesso.SolicitacaoAcesso;
import school.cesar.praxis.domain.acesso.StatusSolicitacao;
import school.cesar.praxis.domain.cliente.Cliente;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.VersaoDocumento;
import school.cesar.praxis.domain.documento.RegistroAprovacao;
import school.cesar.praxis.domain.documento.StatusDocumento;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.DataUnica;
import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.RecorrenciaAnualFixa;
import school.cesar.praxis.domain.feriado.RegraRecorrencia;
import school.cesar.praxis.domain.agenda.Audiencia;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.honorario.BaseCalculo;
import school.cesar.praxis.domain.honorario.ContratoHonorario;
import school.cesar.praxis.domain.honorario.Parcela;
import school.cesar.praxis.domain.honorario.RegistroHonorario;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;
import school.cesar.praxis.domain.modelo.TextoModelo;
import school.cesar.praxis.domain.partecontraria.ParteContraria;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.domain.prazo.NivelAlerta;
import school.cesar.praxis.domain.prazo.PeriodoSuspensao;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;
import school.cesar.praxis.domain.processo.*;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.infrastructure.persistence.entity.AdvogadoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.AndamentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.AudienciaEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ArquivoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ClienteEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ContratoHonorarioEntity;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoVersaoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.EscritorioEntity;
import school.cesar.praxis.infrastructure.persistence.entity.FeriadoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ModeloEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ParcelaEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ParteContrariaEntity;
import school.cesar.praxis.infrastructure.persistence.entity.PeriodoSuspensaoEmbeddable;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEventoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.VarreduraExecucaoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.AcessoAoProcessoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.SolicitacaoAcessoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ProcessoEntity;
import school.cesar.praxis.infrastructure.persistence.entity.UsuarioEntity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        if (processo.getVara() != null) {
            entidade.setVaraTribunal(processo.getVara().tribunal());
            entidade.setVaraComarca(processo.getVara().comarca());
            entidade.setVaraNumero(processo.getVara().numero());
        }

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
        Vara vara = entidade.getVaraTribunal() == null && entidade.getVaraNumero() == null
                ? null
                : new Vara(entidade.getVaraTribunal(), entidade.getVaraComarca(), entidade.getVaraNumero());

        Processo processo = new Processo(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroCnj()),
                entidade.getCliente(),
                entidade.getComarca(),
                entidade.isSegredoJustica(),
                new Advogado(entidade.getResponsavelNome(),
                        entidade.getResponsavelEmail(),
                        entidade.getResponsavelOab()),
                vara);

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
        entidade.setPrerrogativa(prazo.getPrerrogativa());
        entidade.setVencimentoOriginal(prazo.getVencimentoOriginal());
        entidade.setVencimento(prazo.getVencimento());
        entidade.setDiasAcrescidos(prazo.getDiasAcrescidos());
        entidade.setFatal(prazo.isFatal());
        entidade.setResponsavelNome(prazo.getResponsavel().nome());
        entidade.setResponsavelEmail(prazo.getResponsavel().email());
        entidade.setResponsavelOab(prazo.getResponsavel().oab());
        entidade.setAlertasEmitidos(prazo.getAlertasEmitidos().stream()
                .map(Enum::name)
                .collect(Collectors.joining(",")));
        entidade.setSituacao(prazo.getSituacao());
        entidade.setCumprido(prazo.isCumprido());
        entidade.setCumpridoEm(prazo.getCumpridoEm());
        entidade.setSuspensoes(prazo.getSuspensoes().stream()
                .map(periodo -> new PeriodoSuspensaoEmbeddable(periodo.inicio(), periodo.fim(), periodo.motivo()))
                .collect(Collectors.toCollection(ArrayList::new)));
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

        // Linha gravada antes da V7 nao tem situacao: o booleano antigo decide.
        SituacaoPrazo situacao = entidade.getSituacao() != null
                ? entidade.getSituacao()
                : entidade.isCumprido() ? SituacaoPrazo.CUMPRIDO : SituacaoPrazo.EM_ABERTO;
        List<PeriodoSuspensao> suspensoes = entidade.getSuspensoes() == null
                ? List.of()
                : entidade.getSuspensoes().stream()
                        .map(periodo -> new PeriodoSuspensao(periodo.getInicio(), periodo.getFim(), periodo.getMotivo()))
                        .sorted(java.util.Comparator.comparing(PeriodoSuspensao::inicio))
                        .toList();

        return new Prazo(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getDescricao(),
                entidade.getIntimacao(),
                entidade.getQuantidadeDias(),
                entidade.getRegime(),
                entidade.getPrerrogativa(),
                entidade.getVencimentoOriginal(),
                entidade.getVencimento(),
                entidade.isFatal(),
                new Advogado(entidade.getResponsavelNome(),
                        entidade.getResponsavelEmail(),
                        entidade.getResponsavelOab()),
                alertas,
                situacao,
                entidade.getCumpridoEm(),
                entidade.getDiasAcrescidos(),
                suspensoes);
    }

    // --- Trilha do prazo e execucoes da varredura ---

    public static PrazoEventoEntity paraEntidade(EventoPrazo evento) {
        PrazoEventoEntity entidade = new PrazoEventoEntity();
        entidade.setId(evento.id());
        entidade.setPrazoId(evento.prazoId());
        entidade.setTipo(evento.tipo());
        entidade.setOcorridoEm(evento.ocorridoEm());
        entidade.setVencimentoAnterior(evento.vencimentoAnterior());
        entidade.setVencimentoNovo(evento.vencimentoNovo());
        entidade.setDetalhe(limitar(evento.detalhe(), 500));
        entidade.setAutorOab(evento.autorOab());
        entidade.setAutorNome(limitar(evento.autorNome(), 150));
        return entidade;
    }

    public static EventoPrazo paraDominio(PrazoEventoEntity entidade) {
        return new EventoPrazo(
                entidade.getId(),
                entidade.getPrazoId(),
                entidade.getTipo(),
                entidade.getOcorridoEm(),
                entidade.getVencimentoAnterior(),
                entidade.getVencimentoNovo(),
                entidade.getDetalhe(),
                entidade.getAutorOab(),
                entidade.getAutorNome());
    }

    public static VarreduraExecucaoEntity paraEntidade(ExecucaoVarredura execucao) {
        VarreduraExecucaoEntity entidade = new VarreduraExecucaoEntity();
        entidade.setId(execucao.id());
        entidade.setOrigem(execucao.origem());
        entidade.setDataReferencia(execucao.dataReferencia());
        entidade.setIniciadaEm(execucao.iniciadaEm());
        entidade.setConcluidaEm(execucao.concluidaEm());
        entidade.setPrazosAvaliados(execucao.prazosAvaliados());
        entidade.setAlertasEmitidos(execucao.alertasEmitidos());
        entidade.setSolicitante(limitar(execucao.solicitante(), 150));
        return entidade;
    }

    public static ExecucaoVarredura paraDominio(VarreduraExecucaoEntity entidade) {
        return new ExecucaoVarredura(
                entidade.getId(),
                entidade.getOrigem(),
                entidade.getDataReferencia(),
                entidade.getIniciadaEm(),
                entidade.getConcluidaEm(),
                entidade.getPrazosAvaliados(),
                entidade.getAlertasEmitidos(),
                entidade.getSolicitante());
    }

    /** Texto livre que vai para coluna de tamanho fixo: corta em vez de estourar o insert. */
    private static String limitar(String texto, int maximo) {
        if (texto == null || texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 1) + "…";
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
        entidade.setModeloCodigo(documento.getModeloCodigo());
        entidade.setCamposGeracao(serializarCampos(documento.getCamposGeracao()));
        entidade.setVersaoAtual(documento.getVersaoAtual());
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
                oabs,
                entidade.getModeloCodigo(),
                desserializarCampos(entidade.getCamposGeracao()),
                Math.max(1, entidade.getVersaoAtual()));
        documento.restaurarStatusPersistido(StatusDocumento.porNome(entidade.getStatus()));
        documento.restaurarHistoricoPersistido(desserializarHistorico(entidade.getHistorico()));
        return documento;
    }

    /**
     * Historico em coluna texto: campos separados por '|' e registros por ';'.
     * Cada campo e percent-encoded, entao um comentario "faltou procuracao; refazer"
     * nao quebra a leitura de todo o documento.
     */
    private static String serializarHistorico(List<RegistroAprovacao> historico) {
        return historico.stream()
                .map(r -> String.join("|",
                        codificar(r.getDeEstado()),
                        codificar(r.getParaEstado()),
                        codificar(r.getResponsavelOab()),
                        codificar(r.getComentario()),
                        codificar(r.getQuando().toString())))
                .collect(Collectors.joining(";"));
    }

    private static List<RegistroAprovacao> desserializarHistorico(String valor) {
        List<RegistroAprovacao> historico = new ArrayList<>();
        if (valor == null || valor.isBlank()) {
            return historico;
        }
        for (String entrada : valor.split(";")) {
            String[] partes = entrada.split("\\|", -1);
            if (partes.length < 5) {
                continue; // registro truncado por versao anterior: nao derruba a leitura
            }
            historico.add(new RegistroAprovacao(
                    decodificar(partes[0]),
                    decodificar(partes[1]),
                    decodificar(partes[2]),
                    decodificar(partes[3]),
                    LocalDateTime.parse(decodificar(partes[4]))));
        }
        return historico;
    }

    /** Campos da geracao: "chave=valor" percent-encoded, separados por '&' (ordem preservada). */
    private static String serializarCampos(java.util.Map<String, String> campos) {
        if (campos == null || campos.isEmpty()) {
            return null;
        }
        return campos.entrySet().stream()
                .map(campo -> codificar(campo.getKey()) + "=" + codificar(campo.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static java.util.Map<String, String> desserializarCampos(String valor) {
        java.util.Map<String, String> campos = new java.util.LinkedHashMap<>();
        if (valor == null || valor.isBlank()) {
            return campos;
        }
        for (String par : valor.split("&")) {
            int separador = par.indexOf('=');
            if (separador > 0) {
                campos.put(decodificar(par.substring(0, separador)),
                        par.length() > separador + 1 ? decodificar(par.substring(separador + 1)) : "");
            }
        }
        return campos;
    }

    // --- Versao do documento ---

    public static DocumentoVersaoEntity paraEntidade(VersaoDocumento versao) {
        DocumentoVersaoEntity entidade = new DocumentoVersaoEntity();
        entidade.setId(versao.id());
        entidade.setDocumentoId(versao.documentoId());
        entidade.setNumero(versao.numero());
        entidade.setConteudo(versao.conteudo());
        entidade.setOrigem(versao.origem());
        entidade.setAutorOab(versao.autorOab());
        entidade.setAutorNome(limitar(versao.autorNome(), 150));
        entidade.setCriadaEm(versao.criadaEm());
        entidade.setResumo(limitar(versao.resumo(), 300));
        return entidade;
    }

    public static VersaoDocumento paraDominio(DocumentoVersaoEntity entidade) {
        return new VersaoDocumento(
                entidade.getId(),
                entidade.getDocumentoId(),
                entidade.getNumero(),
                entidade.getConteudo(),
                entidade.getOrigem(),
                entidade.getAutorOab(),
                entidade.getAutorNome(),
                entidade.getCriadaEm(),
                entidade.getResumo());
    }

    private static String codificar(String valor) {
        return valor == null ? "" : URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static String decodificar(String valor) {
        return valor == null || valor.isEmpty() ? null : URLDecoder.decode(valor, StandardCharsets.UTF_8);
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

    // --- Cliente ---

    public static ClienteEntity paraEntidade(Cliente cliente) {
        ClienteEntity entidade = new ClienteEntity();
        entidade.setId(cliente.getId());
        entidade.setNome(cliente.getNome());
        entidade.setCpfOuCnpj(cliente.getCpfOuCnpj());
        entidade.setTipoPessoa(cliente.getTipoPessoa());
        entidade.setEmail(cliente.getEmail());
        entidade.setTelefone(cliente.getTelefone());
        entidade.setCelular(cliente.getCelular());
        entidade.setEndereco(cliente.getEndereco());
        entidade.setCidade(cliente.getCidade());
        entidade.setEstado(cliente.getEstado());
        entidade.setCep(cliente.getCep());
        entidade.setProfissao(cliente.getProfissao());
        entidade.setEmpresaTrabalho(cliente.getEmpresaTrabalho());
        entidade.setObservacoes(cliente.getObservacoes());
        entidade.setAtivo(cliente.isAtivo());
        entidade.setCriadoEm(cliente.getCriadoEm());
        entidade.setAtualizadoEm(cliente.getAtualizadoEm());
        return entidade;
    }

    public static Cliente paraDominio(ClienteEntity entidade) {
        return new Cliente(
                entidade.getId(),
                entidade.getNome(),
                entidade.getCpfOuCnpj(),
                entidade.getTipoPessoa(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getCelular(),
                entidade.getEndereco(),
                entidade.getCidade(),
                entidade.getEstado(),
                entidade.getCep(),
                entidade.getProfissao(),
                entidade.getEmpresaTrabalho(),
                entidade.getObservacoes(),
                entidade.isAtivo(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }

    // --- Parte contraria ---

    public static ParteContrariaEntity paraEntidade(ParteContraria parteContraria) {
        ParteContrariaEntity entidade = new ParteContrariaEntity();
        entidade.setId(parteContraria.getId());
        entidade.setNome(parteContraria.getNome());
        entidade.setCpfOuCnpj(parteContraria.getCpfOuCnpj());
        entidade.setTipoPessoa(parteContraria.getTipoPessoa());
        entidade.setEmail(parteContraria.getEmail());
        entidade.setTelefone(parteContraria.getTelefone());
        entidade.setEndereco(parteContraria.getEndereco());
        entidade.setCidade(parteContraria.getCidade());
        entidade.setEstado(parteContraria.getEstado());
        entidade.setCep(parteContraria.getCep());
        entidade.setObservacoes(parteContraria.getObservacoes());
        entidade.setAtiva(parteContraria.isAtiva());
        entidade.setCriadaEm(parteContraria.getCriadaEm());
        entidade.setAtualizadaEm(parteContraria.getAtualizadaEm());
        return entidade;
    }

    public static ParteContraria paraDominio(ParteContrariaEntity entidade) {
        return new ParteContraria(
                entidade.getId(),
                entidade.getNome(),
                entidade.getCpfOuCnpj(),
                entidade.getTipoPessoa(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getEndereco(),
                entidade.getCidade(),
                entidade.getEstado(),
                entidade.getCep(),
                entidade.getObservacoes(),
                entidade.isAtiva(),
                entidade.getCriadaEm(),
                entidade.getAtualizadaEm());
    }

    // --- Audiencia ---

    public static AudienciaEntity paraEntidade(Audiencia audiencia) {
        AudienciaEntity entidade = new AudienciaEntity();
        entidade.setId(audiencia.getId());
        entidade.setNumeroProcesso(audiencia.getNumeroProcesso());
        entidade.setNomeParteAutora(audiencia.getNomeParteAutora());
        entidade.setDataHoraInicio(audiencia.getDataHoraInicio());
        entidade.setDataHoraFim(audiencia.getDataHoraFim());
        entidade.setSala(audiencia.getSala());
        entidade.setObservacoes(audiencia.getObservacoes());
        entidade.setAtiva(audiencia.isAtiva());
        entidade.setCriadaEm(audiencia.getCriadaEm());
        entidade.setAtualizadaEm(audiencia.getAtualizadaEm());
        return entidade;
    }

    public static Audiencia paraDominio(AudienciaEntity entidade) {
        return new Audiencia(
                entidade.getId(),
                entidade.getNumeroProcesso(),
                entidade.getNomeParteAutora(),
                entidade.getDataHoraInicio(),
                entidade.getDataHoraFim(),
                entidade.getSala(),
                entidade.getObservacoes(),
                entidade.isAtiva(),
                entidade.getCriadaEm(),
                entidade.getAtualizadaEm());
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

    public static ContratoHonorarioEntity paraEntidade(ContratoHonorario contrato) {
        BaseCalculo base = contrato.getBaseCalculo();
        ContratoHonorarioEntity entidade = new ContratoHonorarioEntity();
        entidade.setId(contrato.getId());
        entidade.setNumeroProcesso(contrato.getNumeroProcesso().valor());
        entidade.setModalidade(contrato.getModalidade());
        entidade.setCelebradoEm(contrato.getCelebradoEm());
        entidade.setValorFixo(base.valorFixo());
        entidade.setValorHora(base.valorHora());
        entidade.setHorasTrabalhadas(base.horasTrabalhadas());
        entidade.setValorCausa(base.valorCausa());
        entidade.setPercentualExito(base.percentualExito());
        entidade.setValorContratado(contrato.getValorContratado());
        entidade.setQuantidadeParcelas(contrato.getQuantidadeParcelas());
        entidade.setPrimeiroVencimento(contrato.getPrimeiroVencimento());
        entidade.setValorObtido(contrato.getValorObtido());
        entidade.setHonorariosSucumbencia(contrato.getHonorariosSucumbencia());
        entidade.setHistorico(serializarHistoricoHonorario(contrato.getHistorico()));
        entidade.setParcelas(contrato.getParcelas().stream()
                .map(PersistenciaMapper::paraEntidade)
                .collect(Collectors.toList()));
        return entidade;
    }

    public static ContratoHonorario paraDominio(ContratoHonorarioEntity entidade) {
        List<Parcela> parcelas = entidade.getParcelas().stream()
                .map(PersistenciaMapper::paraDominio)
                .sorted(java.util.Comparator.comparingInt(Parcela::getNumero))
                .toList();
        return new ContratoHonorario(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getModalidade(),
                new BaseCalculo(
                        entidade.getValorFixo(),
                        entidade.getValorHora(),
                        entidade.getHorasTrabalhadas(),
                        entidade.getValorCausa(),
                        entidade.getPercentualExito()),
                entidade.getCelebradoEm(),
                entidade.getValorContratado(),
                entidade.getQuantidadeParcelas(),
                entidade.getPrimeiroVencimento(),
                parcelas,
                entidade.getValorObtido(),
                entidade.getHonorariosSucumbencia(),
                desserializarHistoricoHonorario(entidade.getHistorico()));
    }

    private static ParcelaEntity paraEntidade(Parcela parcela) {
        ParcelaEntity entidade = new ParcelaEntity();
        entidade.setNumero(parcela.getNumero());
        entidade.setVencimento(parcela.getVencimento());
        entidade.setValor(parcela.getValor());
        entidade.setValorPago(parcela.getValorPago());
        entidade.setDataPagamento(parcela.getDataPagamento());
        return entidade;
    }

    private static Parcela paraDominio(ParcelaEntity entidade) {
        return new Parcela(entidade.getNumero(), entidade.getVencimento(), entidade.getValor(),
                entidade.getValorPago(), entidade.getDataPagamento());
    }

    private static String serializarHistoricoHonorario(List<RegistroHonorario> historico) {
        return historico.stream()
                .map(r -> String.join("|", r.getTipo(), r.getDescricao(), r.getQuando().toString()))
                .collect(Collectors.joining(";"));
    }

    private static List<RegistroHonorario> desserializarHistoricoHonorario(String valor) {
        List<RegistroHonorario> historico = new ArrayList<>();
        if (valor == null || valor.isBlank()) {
            return historico;
        }
        for (String entrada : valor.split(";")) {
            String[] partes = entrada.split("\\|", -1);
            historico.add(new RegistroHonorario(partes[0], partes[1], LocalDateTime.parse(partes[2])));
        }
        return historico;
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

    // --- Usuario ---

    public static UsuarioEntity paraEntidade(Usuario usuario) {
        UsuarioEntity entidade = new UsuarioEntity();
        entidade.setId(usuario.getId());
        entidade.setNome(usuario.getNome());
        entidade.setEmail(usuario.getEmail());
        entidade.setOab(usuario.getOab());
        entidade.setPapel(usuario.getPapel());
        entidade.setSenhaCodificada(usuario.getSenhaCodificada());
        entidade.setSenhaProvisoria(usuario.isSenhaProvisoria());
        return entidade;
    }

    public static Usuario paraDominio(UsuarioEntity entidade) {
        return new Usuario(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getOab(),
                entidade.getPapel(),
                entidade.getSenhaCodificada(),
                entidade.isSenhaProvisoria());
    }

    // --- Escritorio ---

    public static EscritorioEntity paraEntidade(Escritorio escritorio) {
        EscritorioEntity entidade = new EscritorioEntity();
        entidade.setId(escritorio.getId());
        entidade.setNome(escritorio.getNome());
        entidade.setCnpj(escritorio.getCnpj());
        entidade.setEmail(escritorio.getEmail());
        entidade.setTelefone(escritorio.getTelefone());
        entidade.setUf(escritorio.getUf());
        entidade.setComarca(escritorio.getComarca());
        entidade.setSenhaCodificada(escritorio.getSenhaCodificada());
        return entidade;
    }

    public static Escritorio paraDominio(EscritorioEntity entidade) {
        return new Escritorio(
                entidade.getId(),
                entidade.getNome(),
                entidade.getCnpj(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getUf(),
                entidade.getComarca(),
                entidade.getSenhaCodificada());
    }

    // --- Advogado ---

    public static AdvogadoEntity paraEntidade(school.cesar.praxis.domain.advogado.Advogado advogado) {
        AdvogadoEntity entidade = new AdvogadoEntity();
        entidade.setId(advogado.getId());
        entidade.setNome(advogado.getNome());
        entidade.setEmail(advogado.getEmail());
        entidade.setOab(advogado.getOab());
        entidade.setTelefone(advogado.getTelefone());
        entidade.setEspecialidade(advogado.getEspecialidade());
        entidade.setStatus(advogado.getStatus());
        entidade.setDisponivel(advogado.isDisponivel());
        entidade.setDataAdmissao(advogado.getDataAdmissao());
        return entidade;
    }

    public static school.cesar.praxis.domain.advogado.Advogado paraDominio(AdvogadoEntity entidade) {
        return new school.cesar.praxis.domain.advogado.Advogado(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getOab(),
                entidade.getTelefone(),
                entidade.getEspecialidade(),
                entidade.getStatus(),
                entidade.isDisponivel(),
                entidade.getDataAdmissao());
    }

    // --- AcessoAoProcesso ---

    public static AcessoAoProcessoEntity paraEntidade(AcessoAoProcesso acesso) {
        AcessoAoProcessoEntity entidade = new AcessoAoProcessoEntity();
        entidade.setId(acesso.getId());
        entidade.setNumeroProcesso(acesso.getNumeroProcesso().valor());
        entidade.setOab(acesso.getOab());
        entidade.setConcedidoPor(acesso.getConcedidoPor());
        entidade.setConcedidoEm(acesso.getConcedidoEm());
        entidade.setExpiraEm(acesso.getExpiraEm());
        entidade.setRevogadoEm(acesso.getRevogadoEm());
        entidade.setMotivoRevogacao(acesso.getMotivoRevogacao());
        return entidade;
    }

    public static AcessoAoProcesso paraDominio(AcessoAoProcessoEntity entidade) {
        return new AcessoAoProcesso(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getOab(),
                entidade.getConcedidoPor(),
                entidade.getConcedidoEm(),
                entidade.getExpiraEm(),
                entidade.getRevogadoEm(),
                entidade.getMotivoRevogacao());
    }

    // --- SolicitacaoAcesso ---

    public static SolicitacaoAcessoEntity paraEntidade(SolicitacaoAcesso solicitacao) {
        SolicitacaoAcessoEntity entidade = new SolicitacaoAcessoEntity();
        entidade.setId(solicitacao.getId());
        entidade.setNumeroProcesso(solicitacao.getNumeroProcesso().valor());
        entidade.setOabSolicitante(solicitacao.getOabSolicitante());
        entidade.setJustificativa(solicitacao.getJustificativa());
        entidade.setSolicitadoEm(solicitacao.getSolicitadoEm());
        entidade.setStatus(solicitacao.getStatus().name());
        entidade.setDecididoEm(solicitacao.getDecididoEm());
        entidade.setDecididoPorOab(solicitacao.getDecididoPorOab());
        entidade.setMotivoNegacao(solicitacao.getMotivoNegacao());
        return entidade;
    }

    public static SolicitacaoAcesso paraDominio(SolicitacaoAcessoEntity entidade) {
        return new SolicitacaoAcesso(
                entidade.getId(),
                NumeroCnj.de(entidade.getNumeroProcesso()),
                entidade.getOabSolicitante(),
                entidade.getJustificativa(),
                entidade.getSolicitadoEm(),
                StatusSolicitacao.valueOf(entidade.getStatus()),
                entidade.getDecididoEm(),
                entidade.getDecididoPorOab(),
                entidade.getMotivoNegacao());
    }
}
