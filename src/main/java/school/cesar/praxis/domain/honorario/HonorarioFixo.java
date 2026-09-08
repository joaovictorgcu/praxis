package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;

public class HonorarioFixo implements CalculoHonorarioStrategy {

    @Override
    public BigDecimal calcular(BaseCalculo base) {
        return base.valorFixo();
    }

    @Override
    public String modalidade() {
        return "FIXO";
    }
}
