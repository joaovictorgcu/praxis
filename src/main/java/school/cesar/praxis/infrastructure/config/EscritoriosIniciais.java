package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.EscritoriosUseCases;
import school.cesar.praxis.application.port.out.EscritorioRepositorio;

/**
 * Escritorio de referencia para o sistema nascer acessivel sem depender do
 * cadastro publico: login e senha conhecidos, so em dev.
 *
 * <p>Roda antes de o servidor abrir a porta ({@code afterSingletonsInstantiated})
 * porque sem escritorio ninguem entra no painel - inclusive nos testes HTTP.
 */
@Component
@ConditionalOnProperty(name = "praxis.escritorios-iniciais", havingValue = "true",
        matchIfMissing = true)
public class EscritoriosIniciais implements SmartInitializingSingleton {

    private final EscritoriosUseCases.CadastrarEscritorio cadastrar;
    private final EscritorioRepositorio escritorios;
    private final String email;
    private final String senha;

    public EscritoriosIniciais(EscritoriosUseCases.CadastrarEscritorio cadastrar,
                               EscritorioRepositorio escritorios,
                               @Value("${praxis.escritorio-inicial.email:contato@praxis.adv.br}") String email,
                               @Value("${praxis.escritorio-inicial.senha:praxis123}") String senha) {
        this.cadastrar = cadastrar;
        this.escritorios = escritorios;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (escritorios.porEmail(email).isPresent()) {
            return;
        }
        cadastrar.executar(new EscritoriosUseCases.CadastrarEscritorio.Comando(
                "Praxis Advocacia", null, email, null, "PE", "Recife", senha));
    }
}
