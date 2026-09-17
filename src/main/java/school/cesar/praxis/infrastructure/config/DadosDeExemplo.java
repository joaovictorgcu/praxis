package school.cesar.praxis.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * Carga inicial para demonstracao: dois processos, andamentos, quatro prazos em
 * estados diferentes (fatal proximo, comum distante, um vencido e um cumprido),
 * uma peca em revisao e o entorno do escritorio - clientes, parte contraria,
 * audiencia, contratos de honorario e um anexo nos autos. Sem isso, metade das
 * telas da administracao abre vazia.
 */
@Component
@ConditionalOnProperty(name = "praxis.dados-exemplo", havingValue = "true", matchIfMissing = true)
public class DadosDeExemplo implements CommandLineRunner {

    private static final String PROCESSO_PUBLICO = "0001234-56.2026.8.17.0001";
    private static final String PROCESSO_SIGILOSO = "0007654-32.2026.8.17.0002";

    private final ProcessosUseCases.CadastrarProcesso cadastrar;
    private final ProcessosUseCases.RegistrarAndamento registrar;
    private final PrazosUseCases.AbrirPrazo abrirPrazo;
    private final DocumentosUseCases.GerarDocumento gerarDocumento;
    private final DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;
    private final PrazosUseCases.CumprirPrazo cumprirPrazo;
    private final AnexosUseCases.AnexarArquivo anexar;
    private final ClienteUseCase clientes;
    private final ParteContrariaUseCase partesContrarias;
    private final AgendaDeAudienciasUseCase audiencias;
    private final HonorariosUseCases.CadastrarContrato contratos;
    private final ProcessoRepositorio processos;
    private final Relogio relogio;

    public DadosDeExemplo(ProcessosUseCases.CadastrarProcesso cadastrar,
                          ProcessosUseCases.RegistrarAndamento registrar,
                          PrazosUseCases.AbrirPrazo abrirPrazo,
                          DocumentosUseCases.GerarDocumento gerarDocumento,
                          DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao,
                          PrazosUseCases.CumprirPrazo cumprirPrazo,
                          AnexosUseCases.AnexarArquivo anexar,
                          ClienteUseCase clientes,
                          ParteContrariaUseCase partesContrarias,
                          AgendaDeAudienciasUseCase audiencias,
                          HonorariosUseCases.CadastrarContrato contratos,
                          ProcessoRepositorio processos,
                          Relogio relogio) {
        this.cadastrar = cadastrar;
        this.registrar = registrar;
        this.abrirPrazo = abrirPrazo;
        this.gerarDocumento = gerarDocumento;
        this.enviarParaRevisao = enviarParaRevisao;
        this.cumprirPrazo = cumprirPrazo;
        this.anexar = anexar;
        this.clientes = clientes;
        this.partesContrarias = partesContrarias;
        this.audiencias = audiencias;
        this.contratos = contratos;
        this.processos = processos;
        this.relogio = relogio;
    }

    @Override
    public void run(String... args) {
        LocalDate hoje = relogio.hoje();
        if (processos.porNumero(NumeroCnj.de(PROCESSO_PUBLICO)).isEmpty()) {
            carregarNucleo(hoje);
        }
        carregarCarteira(hoje);
    }

    private void carregarNucleo(LocalDate hoje) {
        cadastrar.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                PROCESSO_PUBLICO, "Construtora Alfa Ltda.", "Recife", false,
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345"));

        cadastrar.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                PROCESSO_SIGILOSO, "M. R. S.", "Olinda", true,
                "Bruno Carvalho", "bruno.carvalho@praxis.adv.br", "PE54321"));

        registrar.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                PROCESSO_PUBLICO, hoje.minusDays(20), "Distribuicao da acao", TipoAndamento.OUTRO));
        registrar.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                PROCESSO_PUBLICO, hoje.minusDays(4), "Intimacao para contestar", TipoAndamento.INTIMACAO));
        registrar.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                PROCESSO_SIGILOSO, hoje.minusDays(9), "Citacao da parte re", TipoAndamento.CITACAO));

        abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                PROCESSO_PUBLICO, "Contestacao", hoje.minusDays(4), 15, true, RegimeContagem.DIAS_UTEIS));
        abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                PROCESSO_PUBLICO, "Manifestacao sobre laudo", hoje.minusDays(1), 10, false, RegimeContagem.DIAS_UTEIS));
        abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                PROCESSO_SIGILOSO, "Embargos de declaracao", hoje.minusDays(9), 5, true, RegimeContagem.DIAS_UTEIS));

        // Prazo ja cumprido: sai da agenda do dia, mas aparece na administracao.
        Prazo juntada = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                PROCESSO_PUBLICO, "Juntada de procuracao", hoje.minusDays(25), 5, false, RegimeContagem.DIAS_UTEIS));
        cumprirPrazo.executar(juntada.getId());

        // Uma peca ja em revisao, para o chefe encontrar trabalho ao entrar.
        DocumentoGerado contestacao = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                PROCESSO_PUBLICO, TipoDocumento.CONTESTACAO,
                Map.of("preliminares", "Ilegitimidade passiva: a re nao figura no contrato de locacao.",
                        "merito", "A re nega o inadimplemento alegado; os alugueis foram pagos (art. 336 do CPC)."),
                "PE12345"));
        enviarParaRevisao.executar(new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(contestacao.getId()));

        entornoDoEscritorio(hoje);
    }

    /**
     * Carteira do escritorio: processos com prazos em todos os estagios de
     * alerta, para a agenda e a administracao terem volume parecido com o de um
     * escritorio pequeno em funcionamento.
     *
     * <p>Roda processo a processo, e nao de uma vez como {@link #carregarNucleo}:
     * numa base que ja existe, so entra o que ainda falta. Sao dezenas de linhas
     * curtas - a ordem de grandeza e de dezenas de KB, longe do limite de
     * qualquer banco gratuito. Volume mesmo esta nos anexos, que sao binarios e
     * continuam sendo um so.
     */
    private void carregarCarteira(LocalDate hoje) {
        // numero CNJ, cliente, comarca, segredo, responsavel, e-mail, OAB
        processo("0002345-77.2026.8.17.0001", "Mercadinho Sao Jose Ltda.", "Recife", false,
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345",
                hoje.minusDays(12), "Intimacao para replica", TipoAndamento.INTIMACAO,
                // descricao, intimacao (dias atras), dias, fatal, regime, cumprido
                prazo("Replica a contestacao", 12, 15, true, RegimeContagem.DIAS_UTEIS, false),
                prazo("Especificacao de provas", 3, 5, false, RegimeContagem.DIAS_UTEIS, false));

        processo("0003456-93.2026.8.17.0002", "Helena Barros", "Olinda", false,
                "Bruno Carvalho", "bruno.carvalho@praxis.adv.br", "PE54321",
                hoje.minusDays(6), "Sentenca publicada", TipoAndamento.SENTENCA,
                prazo("Apelacao", 6, 15, true, RegimeContagem.DIAS_UTEIS, false),
                prazo("Custas de preparo", 6, 10, false, RegimeContagem.DIAS_CORRIDOS, false));

        processo("0004567-12.2026.8.17.0003", "Transportes Norte S.A.", "Jaboatao dos Guararapes", false,
                "Carla Mendes", "carla.mendes@praxis.adv.br", "PE00001",
                hoje.minusDays(2), "Despacho saneador", TipoAndamento.DESPACHO,
                prazo("Manifestacao sobre saneador", 2, 5, true, RegimeContagem.DIAS_UTEIS, false),
                prazo("Rol de testemunhas", 2, 15, false, RegimeContagem.DIAS_UTEIS, false));

        processo("0005678-28.2026.8.17.0004", "Padaria Dois Irmaos ME", "Recife", false,
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345",
                hoje.minusDays(45), "Intimacao para impugnacao", TipoAndamento.INTIMACAO,
                // Vencido em aberto: o alerta de prazo perdido tem de aparecer na agenda.
                prazo("Impugnacao ao cumprimento de sentenca", 45, 15, true, RegimeContagem.DIAS_UTEIS, false),
                prazo("Juntada de substabelecimento", 40, 5, false, RegimeContagem.DIAS_UTEIS, true));

        processo("0006789-44.2026.8.17.0005", "Condominio Edificio Aurora", "Recife", false,
                "Bruno Carvalho", "bruno.carvalho@praxis.adv.br", "PE54321",
                hoje.minusDays(20), "Audiencia de conciliacao designada", TipoAndamento.AUDIENCIA,
                prazo("Proposta de acordo", 20, 30, false, RegimeContagem.DIAS_CORRIDOS, false),
                prazo("Comprovacao de pagamento", 30, 10, false, RegimeContagem.DIAS_UTEIS, true));

        processo("0008901-80.2026.8.17.0006", "J. P. M.", "Camaragibe", true,
                "Carla Mendes", "carla.mendes@praxis.adv.br", "PE00001",
                hoje.minusDays(1), "Citacao por oficial de justica", TipoAndamento.CITACAO,
                prazo("Contestacao", 1, 15, true, RegimeContagem.DIAS_UTEIS, false));
    }

    /** Descricao de um prazo da carteira, resolvida contra a data de hoje. */
    private record PrazoDemo(String descricao, int diasAtras, int quantidade, boolean fatal,
                             RegimeContagem regime, boolean cumprido) {
    }

    private static PrazoDemo prazo(String descricao, int diasAtras, int quantidade, boolean fatal,
                                   RegimeContagem regime, boolean cumprido) {
        return new PrazoDemo(descricao, diasAtras, quantidade, fatal, regime, cumprido);
    }

    /** Cadastra o processo com um andamento e seus prazos, se ele ainda nao existir. */
    private void processo(String numero, String cliente, String comarca, boolean segredo,
                          String responsavel, String email, String oab,
                          LocalDate dataAndamento, String andamento, TipoAndamento tipo,
                          PrazoDemo... prazos) {
        if (processos.porNumero(NumeroCnj.de(numero)).isPresent()) {
            return;
        }
        cadastrar.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, cliente, comarca, segredo, responsavel, email, oab));
        registrar.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                numero, dataAndamento, andamento, tipo));

        LocalDate hoje = relogio.hoje();
        for (PrazoDemo p : prazos) {
            Prazo aberto = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                    numero, p.descricao(), hoje.minusDays(p.diasAtras()), p.quantidade(),
                    p.fatal(), p.regime()));
            if (p.cumprido()) {
                cumprirPrazo.executar(aberto.getId());
            }
        }
    }

    /**
     * Cliente, parte contraria, audiencia, contrato e anexo: cadastros que o
     * processo referencia por nome, e que so tinham API REST.
     */
    private void entornoDoEscritorio(LocalDate hoje) {
        clientes.criarCliente(cliente("Construtora Alfa Ltda.", "12.345.678/0001-90",
                TipoPessoa.JURIDICA,
                "contato@alfa.com.br", "(81) 3333-1010", "Recife"));
        clientes.criarCliente(cliente("Helena Barros", "123.456.789-09",
                TipoPessoa.FISICA,
                "helena.barros@email.com", "(81) 98888-2020", "Olinda"));

        CriarParteContrariaRequest parte = new CriarParteContrariaRequest(
                "Imobiliaria Beta ME", "98.765.432/0001-10",
                TipoPessoa.JURIDICA);
        parte.setCidade("Recife");
        parte.setEstado("PE");
        partesContrarias.criarParteContraria(parte);

        CriarAudienciaRequest audiencia = new CriarAudienciaRequest();
        audiencia.setNumeroProcesso(PROCESSO_PUBLICO);
        audiencia.setNomeParteAutora("Construtora Alfa Ltda.");
        audiencia.setDataHoraInicio(hoje.plusDays(22).atTime(LocalTime.of(14, 0)));
        audiencia.setDataHoraFim(hoje.plusDays(22).atTime(LocalTime.of(15, 30)));
        audiencia.setSala("Sala 3 - 2a Vara Civel");
        audiencia.setObservacoes("Audiencia de conciliacao");
        audiencias.criarAudiencia(audiencia);

        contratos.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                PROCESSO_PUBLICO, "QUOTA_LITIS", hoje.minusDays(27),
                null, null, 0, new BigDecimal("120000.00"), new BigDecimal("20")));
        contratos.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                PROCESSO_SIGILOSO, "FIXO", hoje.minusDays(15),
                new BigDecimal("8500.00"), null, 0, null, null));

        anexar.executar(new AnexosUseCases.AnexarArquivo.Comando(
                PROCESSO_PUBLICO, "laudo-pericial.pdf", "application/pdf",
                "%PDF-1.4\n% laudo pericial de exemplo\n".getBytes(StandardCharsets.UTF_8),
                "Laudo pericial do engenheiro", "PE12345"));
    }

    private static CriarClienteRequest cliente(String nome, String documento, TipoPessoa tipo,
                                               String email, String telefone, String cidade) {
        CriarClienteRequest request = new CriarClienteRequest(nome, documento, tipo);
        request.setEmail(email);
        request.setTelefone(telefone);
        request.setCidade(cidade);
        request.setEstado("PE");
        return request;
    }
}
