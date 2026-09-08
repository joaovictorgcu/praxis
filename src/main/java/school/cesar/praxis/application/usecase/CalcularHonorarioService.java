package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import school.cesar.praxis.domain.honorario.BaseCalculo;
import school.cesar.praxis.domain.honorario.CalculoHonorarioStrategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso do subdominio Honorarios: seleciona a Strategy pela modalidade
 * contratada. As estrategias sao injetadas, entao uma modalidade nova nao
 * exige alterar este servico.
 */
@Service
public class CalcularHonorarioService {

    private final Map<String, CalculoHonorarioStrategy> estrategias = new HashMap<>();

    public CalcularHonorarioService(List<CalculoHonorarioStrategy> disponiveis) {
        for (CalculoHonorarioStrategy estrategia : disponiveis) {
            estrategias.put(estrategia.modalidade(), estrategia);
        }
    }

    public BigDecimal calcular(String modalidade, BaseCalculo base) {
        CalculoHonorarioStrategy estrategia = estrategias.get(modalidade);
        if (estrategia == null) {
            throw new IllegalArgumentException("modalidade de honorario desconhecida: " + modalidade);
        }
        return estrategia.calcular(base);
    }
}
