package school.cesar.praxis.fixtures;

import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.DistribuicaoUseCases;

import java.util.List;

public class ProcessoFixture {

    public static ProcessosUseCases.CadastrarProcesso.Comando comandoCadastroValido() {
        return new ProcessosUseCases.CadastrarProcesso.Comando(
                "0009999-88.2026.8.17.0001",
                "Cliente Teste",
                "Recife",
                false,
                "Ana Souza",
                "ana@praxis.adv.br",
                "PE12345"
        );
    }

    public static DistribuicaoUseCases.DistribuirProcesso.Comando comandoDistribuicaoValido() {
        return new DistribuicaoUseCases.DistribuirProcesso.Comando(
                "0009999-88.2026.8.17.0001",
                "Trabalhista",
                List.of(new DistribuicaoUseCases.DistribuirProcesso.Candidato(
                        "Ana Souza",
                        "ana@praxis.adv.br",
                        "PE12345",
                        "Trabalhista",
                        2,
                        true
                ))
        );
    }
}