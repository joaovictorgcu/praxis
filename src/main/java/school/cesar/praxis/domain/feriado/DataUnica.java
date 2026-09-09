package school.cesar.praxis.domain.feriado;

import java.time.LocalDate;

/** Strategy concreta: o feriado vale so naquele dia daquele ano. */
public record DataUnica(LocalDate data) implements RegraRecorrencia {

    public DataUnica {
        if (data == null) {
            throw new IllegalArgumentException("data do feriado e obrigatoria");
        }
    }

    @Override
    public boolean incideEm(LocalDate outra) {
        return data.equals(outra);
    }

    @Override
    public LocalDate dataDeReferencia() {
        return data;
    }

    @Override
    public String rotulo() {
        return "em " + data;
    }
}
