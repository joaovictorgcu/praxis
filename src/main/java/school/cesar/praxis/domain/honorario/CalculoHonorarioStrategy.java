package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;

/** Strategy: cada modalidade de honorario tem formula e base de calculo propria. */
public interface CalculoHonorarioStrategy {

    BigDecimal calcular(BaseCalculo base);

    String modalidade();
}
