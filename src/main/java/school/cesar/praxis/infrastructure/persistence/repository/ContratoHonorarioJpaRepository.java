package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.ContratoHonorarioEntity;

import java.util.List;

public interface ContratoHonorarioJpaRepository extends JpaRepository<ContratoHonorarioEntity, Long> {

    List<ContratoHonorarioEntity> findByNumeroProcessoOrderByIdDesc(String numeroProcesso);
}
