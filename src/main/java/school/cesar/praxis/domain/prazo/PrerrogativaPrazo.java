package school.cesar.praxis.domain.prazo;

/**
 * Prerrogativas de prazo em dobro do CPC. O multiplicador se aplica a
 * quantidade de dias informada na abertura; a dilacao judicial (art. 139, VI)
 * soma depois, ja sobre o prazo dobrado.
 *
 * <p>O escritorio usa estas opcoes quando acompanha prazo de quem tem a
 * prerrogativa (a Fazenda do outro lado, por exemplo) ou quando atua por
 * nucleo de pratica juridica conveniado com a Defensoria (art. 186, § 3º).
 */
public enum PrerrogativaPrazo {

    NENHUMA(1, "Prazo simples", null),
    MINISTERIO_PUBLICO(2, "Ministério Público", "art. 180 do CPC"),
    FAZENDA_PUBLICA(2, "Fazenda Pública", "art. 183 do CPC"),
    DEFENSORIA_PUBLICA(2, "Defensoria Pública ou núcleo de prática jurídica", "art. 186, caput e § 3º, do CPC"),
    LITISCONSORTES_AUTOS_FISICOS(2, "Litisconsortes com procuradores distintos (autos físicos)",
            "art. 229 do CPC");

    private final int multiplicador;
    private final String rotulo;
    private final String fundamento;

    PrerrogativaPrazo(int multiplicador, String rotulo, String fundamento) {
        this.multiplicador = multiplicador;
        this.rotulo = rotulo;
        this.fundamento = fundamento;
    }

    public int multiplicador() {
        return multiplicador;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Dispositivo legal; nulo para o prazo simples. */
    public String fundamento() {
        return fundamento;
    }

    public boolean emDobro() {
        return multiplicador > 1;
    }
}
