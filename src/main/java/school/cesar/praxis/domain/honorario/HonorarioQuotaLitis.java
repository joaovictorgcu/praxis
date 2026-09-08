package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quota litis: percentual sobre o proveito economico. Art. 38 do Codigo de Etica
 * da OAB limita a participacao do advogado a 30% do beneficio economico.
 */
public class HonorarioQuotaLitis implements CalculoHonorarioStrategy {

    private static final BigDecimal LIMITE_ETICO = new BigDecimal("30");

    @Override
    public BigDecimal calcular(BaseCalculo base) {
        if (base.percentualExito().compareTo(LIMITE_ETICO) > 0) {
            throw new IllegalArgumentException(
                    "quota litis acima do limite etico de 30%: " + base.percentualExito());
        }
        return base.valorCausa()
                .multiply(base.percentualExito())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String modalidade() {
        return "QUOTA_LITIS";
    }
}
