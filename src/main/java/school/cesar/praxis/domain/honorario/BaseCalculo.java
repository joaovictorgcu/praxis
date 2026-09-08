package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;

/** Value Object com os insumos possiveis de calculo de honorarios. */
public record BaseCalculo(BigDecimal valorFixo,
                          BigDecimal valorHora,
                          int horasTrabalhadas,
                          BigDecimal valorCausa,
                          BigDecimal percentualExito) {

    public static BaseCalculo fixo(BigDecimal valor) {
        return new BaseCalculo(valor, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static BaseCalculo porHora(BigDecimal valorHora, int horas) {
        return new BaseCalculo(BigDecimal.ZERO, valorHora, horas, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static BaseCalculo quotaLitis(BigDecimal valorCausa, BigDecimal percentual) {
        return new BaseCalculo(BigDecimal.ZERO, BigDecimal.ZERO, 0, valorCausa, percentual);
    }
}
