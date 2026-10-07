package school.cesar.praxis.application.port.out;

import java.util.List;
import java.util.Optional;
import school.cesar.praxis.domain.jurisdicao.VaraJudicial;

public interface VaraRepositorio {
    VaraJudicial salvar(VaraJudicial cadastro);
    Optional<VaraJudicial> porId(Long id);
    List<VaraJudicial> listarTodas();
}
