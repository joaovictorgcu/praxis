package school.cesar.praxis.domain.honorario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MotorDeHonorarios {

    private final Map<String, CalculoHonorarioStrategy> estrategias = new HashMap<>();

    public MotorDeHonorarios(List<CalculoHonorarioStrategy> disponiveis) {
        for (CalculoHonorarioStrategy estrategia : disponiveis) {
            estrategias.put(estrategia.modalidade(), estrategia);
        }
    }

    public CalculoHonorarioStrategy estrategiaPara(String modalidade) {
        CalculoHonorarioStrategy estrategia = estrategias.get(modalidade);
        if (estrategia == null) {
            throw new IllegalArgumentException("modalidade de honorario desconhecida: " + modalidade);
        }
        return estrategia;
    }
}
