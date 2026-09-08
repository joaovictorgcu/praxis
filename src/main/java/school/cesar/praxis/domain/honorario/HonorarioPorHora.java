package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;

public class HonorarioPorHora implements CalculoHonorarioStrategy {

    @Override
    public BigDecimal calcular(BaseCalculo base) {
        return base.valorHora().multiply(BigDecimal.valueOf(base.horasTrabalhadas()));
    }

    @Override
    public String modalidade() {
        return "POR_HORA";
    }
}
