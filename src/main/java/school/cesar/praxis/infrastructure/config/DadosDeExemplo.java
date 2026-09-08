package school.cesar.praxis.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.time.LocalDate;

/**
 * Carga inicial para demonstracao: dois processos, andamentos e tres prazos em
 * estados diferentes (fatal proximo, comum distante e um ja vencido).
 */
@Component
@ConditionalOnProperty(name = "praxis.dados-exemplo", havingValue = "true", matchIfMissing = true)
public class DadosDeExemplo implements CommandLineRunner {

    private static final String PROCESSO_PUBLICO = "0001234-56.2026.8.17.0001";
    private static final String PROCESSO_SIGILOSO = "0007654-32.2026.8.17.0002";

    private final ProcessosUseCases.CadastrarProcesso cadastrar;
    private final ProcessosUseCases.RegistrarAndamento registrar;
    private final PrazosUseCases.AbrirPrazo abrirPrazo;
    private final ProcessoRepositorio processos;
    private final Relogio relogio;

    public DadosDeExemplo(ProcessosUseCases.CadastrarProcesso cadastrar,
                          ProcessosUseCases.RegistrarAndamento registrar,
                          PrazosUseCases.AbrirPrazo abrirPrazo,
                          ProcessoRepositorio processos,
                          Relogio relogio) {
        this.cadastrar = cadastrar;
        this.registrar = registrar;
        this.abrirPrazo = abrirPrazo;
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
    }
}
