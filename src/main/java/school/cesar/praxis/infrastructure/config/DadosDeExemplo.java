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
import school.cesar.praxis.domain.cliente.TipoPessoa;
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
        if (processos.porNumero(NumeroCnj.de(PROCESSO_PUBLICO)).isPresent()) {
            return;
        }

        LocalDate hoje = relogio.hoje();

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
                // parte contraria tem enum proprio, de outro bounded context
                school.cesar.praxis.domain.partecontraria.TipoPessoa.JURIDICA);
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
