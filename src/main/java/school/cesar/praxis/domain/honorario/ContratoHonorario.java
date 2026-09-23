package school.cesar.praxis.domain.honorario;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContratoHonorario {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String modalidade;
    private final BaseCalculo baseCalculo;
    private final LocalDate celebradoEm;
    private final BigDecimal valorContratado;

    public ContratoHonorario(NumeroCnj numeroProcesso,
                             LocalDate celebradoEm,
                             BaseCalculo baseCalculo,
                             CalculoHonorarioStrategy calculo) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("contrato de honorário exige processo");
        }
        if (celebradoEm == null) {
            throw new IllegalArgumentException("data de celebração é obrigatória");
        }
        if (baseCalculo == null) {
            throw new IllegalArgumentException("base de cálculo é obrigatória");
        }
        if (calculo == null) {
            throw new IllegalArgumentException("estratégia de cálculo é obrigatória");
        }
        this.id = null;
        this.numeroProcesso = numeroProcesso;
        this.celebradoEm = celebradoEm;
        this.baseCalculo = baseCalculo;
        this.modalidade = calculo.modalidade();
        this.valorContratado = calculo.calcular(baseCalculo);
    }

    public ContratoHonorario(Long id,
                             NumeroCnj numeroProcesso,
                             String modalidade,
                             BaseCalculo baseCalculo,
                             LocalDate celebradoEm,
                             BigDecimal valorContratado) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.modalidade = modalidade;
        this.baseCalculo = baseCalculo;
        this.celebradoEm = celebradoEm;
        this.valorContratado = valorContratado;
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getModalidade() {
        return modalidade;
    }

    public BaseCalculo getBaseCalculo() {
        return baseCalculo;
    }

    public LocalDate getCelebradoEm() {
        return celebradoEm;
    }

    public BigDecimal getValorContratado() {
        return valorContratado;
    }
}
