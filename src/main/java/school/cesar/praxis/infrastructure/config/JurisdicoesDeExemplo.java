package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.ComarcasUseCases;
import school.cesar.praxis.application.port.in.VarasUseCases;

@Component
@ConditionalOnProperty(name = "praxis.dados-exemplo", havingValue = "true", matchIfMissing = true)
public class JurisdicoesDeExemplo implements SmartInitializingSingleton {

    private final ComarcasUseCases.Listar listar;
    private final ComarcasUseCases.Cadastrar comarcas;
    private final VarasUseCases.Cadastrar varas;

    public JurisdicoesDeExemplo(
        ComarcasUseCases.Listar listar,
        ComarcasUseCases.Cadastrar comarcas,
        VarasUseCases.Cadastrar varas
    ) {
        this.listar = listar;
        this.comarcas = comarcas;
        this.varas = varas;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!listar.executar().isEmpty()) {
            return;
        }
        String nota = "Dados de exemplo para explorar o cadastro de comarcas e varas.";
        var recife = comarcas.executar(
            new ComarcasUseCases.Cadastrar.Comando(
                "Comarca do Recife",
                "Recife",
                "PE",
                "Tribunal de Justiça de Pernambuco",
                null,
                null,
                null,
                null,
                nota
            )
        );
        var olinda = comarcas.executar(
            new ComarcasUseCases.Cadastrar.Comando(
                "Comarca de Olinda",
                "Olinda",
                "PE",
                "Tribunal de Justiça de Pernambuco",
                null,
                null,
                null,
                null,
                nota
            )
        );
        varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                recife.id(),
                "1ª Vara Cível",
                "Cível",
                null,
                null,
                null,
                null,
                nota
            )
        );
        varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                recife.id(),
                "Vara de Família",
                "Família e Sucessões",
                null,
                null,
                null,
                null,
                nota
            )
        );
        varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                olinda.id(),
                "Vara Criminal",
                "Criminal",
                null,
                null,
                null,
                null,
                nota
            )
        );
    }
}
