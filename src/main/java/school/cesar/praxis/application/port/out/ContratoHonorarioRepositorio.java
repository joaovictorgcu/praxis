package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.honorario.ContratoHonorario;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;
import java.util.Optional;

public interface ContratoHonorarioRepositorio {

    ContratoHonorario salvar(ContratoHonorario contrato);

    Optional<ContratoHonorario> porId(Long id);

    List<ContratoHonorario> porProcesso(NumeroCnj numero);

    List<ContratoHonorario> listar();
}
