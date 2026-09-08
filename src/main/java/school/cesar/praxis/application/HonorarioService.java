package school.cesar.praxis.application;

import org.springframework.stereotype.Service;
import school.cesar.praxis.domain.honorario.*;

import java.math.BigDecimal;
import java.util.Map;

/** Seleciona a Strategy de honorario pela modalidade contratada. */
@Service
public class HonorarioService {

    private final Map<String, CalculoHonorarioStrategy> estrategias = Map.of(
            "FIXO", new HonorarioFixo(),
            "POR_HORA", new HonorarioPorHora(),
            "QUOTA_LITIS", new HonorarioQuotaLitis());

    public BigDecimal calcular(String modalidade, BaseCalculo base) {
        CalculoHonorarioStrategy estrategia = estrategias.get(modalidade);
        if (estrategia == null) {
            throw new IllegalArgumentException("modalidade de honorario desconhecida: " + modalidade);
        }
        return estrategia.calcular(base);
    }
}
