package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoVersaoEntity;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data das versoes do texto das pecas. */
public interface DocumentoVersaoJpaRepository extends JpaRepository<DocumentoVersaoEntity, Long> {

    List<DocumentoVersaoEntity> findByDocumentoIdOrderByNumeroAsc(Long documentoId);

    Optional<DocumentoVersaoEntity> findByDocumentoIdAndNumero(Long documentoId, int numero);

    /** Um DELETE so, em vez de carregar cada versao (texto inteiro) para remover. */
    @Modifying
    @Query("delete from DocumentoVersaoEntity v where v.documentoId = :documentoId")
    int excluirDoDocumento(@Param("documentoId") Long documentoId);
}
