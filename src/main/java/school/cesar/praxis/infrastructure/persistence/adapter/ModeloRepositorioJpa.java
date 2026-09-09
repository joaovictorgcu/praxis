package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ModeloRepositorio;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ModeloJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida: cadastro de modelos de peca em JPA. */
@Repository
public class ModeloRepositorioJpa implements ModeloRepositorio {

    private final ModeloJpaRepository jpa;

    public ModeloRepositorioJpa(ModeloJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ModeloDocumento salvar(ModeloDocumento modelo) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(modelo)));
    }

    @Override
    public Optional<ModeloDocumento> porCodigo(CodigoModelo codigo) {
        return jpa.findByCodigo(codigo.valor()).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<ModeloDocumento> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<ModeloDocumento> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
    }
}
