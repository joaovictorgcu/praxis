package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.DocumentoJpaRepository;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Adaptador de saida de documentos. Tambem e o objeto real por tras do
 * DocumentoProxy: {@link #carregar(Long)} devolve o documento sem checar
 * permissao, porque a checagem e responsabilidade do Proxy.
 */
@Repository
public class DocumentoRepositorioJpa implements DocumentoRepositorio {

    private final DocumentoJpaRepository jpa;

    public DocumentoRepositorioJpa(DocumentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public DocumentoGerado salvar(DocumentoGerado documento) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(documento)));
    }

    @Override
    public DocumentoGerado carregar(Long documentoId) {
        return jpa.findById(documentoId)
                .map(PersistenciaMapper::paraDominio)
                .orElseThrow(() -> new NoSuchElementException("documento nao encontrado: " + documentoId));
    }

    @Override
    public List<DocumentoGerado> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcessoOrderByIdDesc(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<DocumentoGerado> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
