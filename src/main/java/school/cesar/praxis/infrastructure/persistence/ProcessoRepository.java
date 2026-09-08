package school.cesar.praxis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.domain.processo.Processo;

import java.util.Optional;

public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    Optional<Processo> findByNumeroValor(String numero);
}
