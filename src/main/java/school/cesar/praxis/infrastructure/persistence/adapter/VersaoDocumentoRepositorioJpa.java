package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.VersaoDocumentoRepositorio;
import school.cesar.praxis.domain.documento.VersaoDocumento;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.DocumentoVersaoJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida: versoes do texto das pecas em JPA ({@code documento_versao}). */
@Repository
public class VersaoDocumentoRepositorioJpa implements VersaoDocumentoRepositorio {

    private final DocumentoVersaoJpaRepository jpa;

    public VersaoDocumentoRepositorioJpa(DocumentoVersaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public VersaoDocumento registrar(VersaoDocumento versao) {
        if (versao.id() != null) {
            throw new IllegalArgumentException("versão já gravada não muda: " + versao.id());
        }
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(versao)));
    }

    @Override
    public List<VersaoDocumento> doDocumento(Long documentoId) {
        return jpa.findByDocumentoIdOrderByNumeroAsc(documentoId).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public Optional<VersaoDocumento> versao(Long documentoId, int numero) {
        return jpa.findByDocumentoIdAndNumero(documentoId, numero).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public void excluirDoDocumento(Long documentoId) {
        jpa.excluirDoDocumento(documentoId);
    }

    @Override
    public long contar() {
        return jpa.count();
    }
}
