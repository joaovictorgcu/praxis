package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ArquivoRepositorio;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ArquivoJpaRepository;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Adaptador de saida de anexos. Tambem e o objeto real por tras do
 * ArquivoProxy: {@link #carregar(Long)} devolve o anexo sem checar permissao,
 * porque a checagem e responsabilidade do Proxy.
 */
@Repository
public class ArquivoRepositorioJpa implements ArquivoRepositorio {

    private final ArquivoJpaRepository jpa;

    public ArquivoRepositorioJpa(ArquivoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ArquivoAnexo salvar(ArquivoAnexo anexo) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(anexo)));
    }

    @Override
    public ArquivoAnexo carregar(Long anexoId) {
        return jpa.findById(anexoId)
                .map(PersistenciaMapper::paraDominio)
                .orElseThrow(() -> new NoSuchElementException("anexo nao encontrado: " + anexoId));
    }

    @Override
    public List<ArquivoAnexo> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcessoOrderByIdAsc(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<ArquivoAnexo> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
