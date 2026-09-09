package school.cesar.praxis.domain.feriado;

import java.time.LocalDate;

/**
 * <b>Strategy</b>: um feriado pode valer em uma data unica (ponto facultativo
 * decretado para aquele ano) ou repetir todo ano na mesma data (Natal,
 * Tiradentes). Quem sabe se a data cai no feriado e a regra, nao o agregado.
 */
public interface RegraRecorrencia {

    boolean incideEm(LocalDate data);

    /**
     * Data que representa a regra no cadastro. Serve para exibir e para
     * persistir: na recorrencia anual, so o dia e o mes tem significado.
     */
    LocalDate dataDeReferencia();

    /** Texto curto para a tela de cadastro. */
    String rotulo();
}
