package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.EscritoriosUseCases;
import school.cesar.praxis.application.port.out.EscritorioRepositorio;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.domain.usuario.CodificadorDeSenha;
import school.cesar.praxis.domain.usuario.CredenciaisInvalidasException;

/**
 * Casos de uso de acesso do escritorio. Reaproveita o mesmo
 * {@link CodificadorDeSenha} e a mesma excecao de credenciais do login de
 * usuario: e o mesmo subdominio generico de Acesso, so a raiz de agregado
 * muda.
 */
@Service
public class EscritorioAppService implements
        EscritoriosUseCases.Autenticar,
        EscritoriosUseCases.CadastrarEscritorio {

    private final EscritorioRepositorio escritorios;
    private final CodificadorDeSenha codificador;

    public EscritorioAppService(EscritorioRepositorio escritorios, CodificadorDeSenha codificador) {
        this.escritorios = escritorios;
        this.codificador = codificador;
    }

    @Override
    @Transactional(readOnly = true)
    public Escritorio executar(EscritoriosUseCases.Autenticar.Comando comando) {
        if (comando.email() == null || comando.email().isBlank()) {
            throw new CredenciaisInvalidasException();
        }
        Escritorio escritorio = escritorios.porEmail(Escritorio.normalizarEmail(comando.email()))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!escritorio.senhaConfere(comando.senha(), codificador)) {
            throw new CredenciaisInvalidasException();
        }
        return escritorio;
    }

    @Override
    @Transactional
    public Escritorio executar(EscritoriosUseCases.CadastrarEscritorio.Comando comando) {
        Escritorio novo = Escritorio.novo(comando.nome(), comando.cnpj(), comando.email(),
                comando.telefone(), comando.uf(), comando.comarca(), comando.senha(), codificador);
        if (escritorios.porEmail(novo.getEmail()).isPresent()) {
            throw new IllegalArgumentException("ja existe escritorio cadastrado com o e-mail " + novo.getEmail());
        }
        return escritorios.salvar(novo);
    }
}
