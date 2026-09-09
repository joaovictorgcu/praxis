package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;

import java.util.List;
import java.util.Optional;

/** Porta de saida: cadastro de modelos de peca. */
public interface ModeloRepositorio {

    ModeloDocumento salvar(ModeloDocumento modelo);

    Optional<ModeloDocumento> porCodigo(CodigoModelo codigo);

    Optional<ModeloDocumento> porId(Long id);

    List<ModeloDocumento> listar();

    void remover(Long id);
}
