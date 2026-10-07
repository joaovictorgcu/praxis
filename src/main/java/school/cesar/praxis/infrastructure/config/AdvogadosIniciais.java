package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;

/**
 * Advogados de referencia para a distribuicao automatica de processos ter
 * candidato desde o primeiro acesso, sem depender do cadastro pela tela
 * {@code /painel/advogados}. Mesmas OABs de {@link UsuariosIniciais}, ja que
 * sao os mesmos profissionais do escritorio.
 *
 * <p>Roda antes de o servidor abrir a porta (ver {@code afterSingletonsInstantiated}),
 * inclusive nos testes HTTP que exercitam a distribuicao.
 */
@Component
@ConditionalOnProperty(name = "praxis.advogados-iniciais", havingValue = "true",
        matchIfMissing = true)
public class AdvogadosIniciais implements SmartInitializingSingleton {

    private final AdvogadosUseCases.CadastrarAdvogado cadastrar;
    private final AdvogadoRepositorio advogados;

    public AdvogadosIniciais(AdvogadosUseCases.CadastrarAdvogado cadastrar, AdvogadoRepositorio advogados) {
        this.cadastrar = cadastrar;
        this.advogados = advogados;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!advogados.listarTodos().isEmpty()) {
            return;
        }
        cadastrar.executar(new AdvogadosUseCases.CadastrarAdvogado.Comando(
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345", null, "Trabalhista", true));
        cadastrar.executar(new AdvogadosUseCases.CadastrarAdvogado.Comando(
                "Bruno Carvalho", "bruno.carvalho@praxis.adv.br", "PE54321", null, "Civil", true));
    }
}
