package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.out.ModeloRepositorio;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;
import school.cesar.praxis.domain.modelo.TextoModelo;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do cadastro de modelos. A analise dos marcadores e a validacao
 * ficam no dominio; aqui so ha traducao do comando e orquestracao.
 */
@Service
public class ModeloAppService implements ModelosUseCases.CadastrarModelo,
        ModelosUseCases.ListarModelos,
        ModelosUseCases.RemoverModelo {

    private final ModeloRepositorio modelos;

    public ModeloAppService(ModeloRepositorio modelos) {
        this.modelos = modelos;
    }

    @Override
    @Transactional
    public ModelosUseCases.ItemModelo executar(Comando comando) {
        CodigoModelo codigo = CodigoModelo.de(comando.codigo());
        if (modelos.porCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException("ja existe modelo com o codigo " + codigo);
        }

        ModeloDocumento salvo = modelos.salvar(new ModeloDocumento(
                codigo,
                comando.nome(),
                comando.tipo(),
                comando.titulo(),
                new TextoModelo(comando.corpo()),
                new TextoModelo(comando.pedidos()),
                comando.enderecaAoJuizo()));

        return paraItem(salvo);
    }

    @Override
    public List<ModelosUseCases.ItemModelo> executar() {
        return modelos.listar().stream()
                .sorted(Comparator.comparing(modelo -> modelo.getCodigo().valor()))
                .map(ModeloAppService::paraItem)
                .toList();
    }

    @Override
    @Transactional
    public void executar(Long id) {
        if (modelos.porId(id).isEmpty()) {
            throw new NoSuchElementException("modelo nao encontrado: " + id);
        }
        modelos.remover(id);
    }

    private static ModelosUseCases.ItemModelo paraItem(ModeloDocumento modelo) {
        return new ModelosUseCases.ItemModelo(
                modelo.getId(),
                modelo.getCodigo().valor(),
                modelo.getNome(),
                modelo.getTipo(),
                modelo.getTipo().rotulo(),
                modelo.getTitulo(),
                modelo.isEnderecaAoJuizo(),
                modelo.camposEsperados());
    }
}
