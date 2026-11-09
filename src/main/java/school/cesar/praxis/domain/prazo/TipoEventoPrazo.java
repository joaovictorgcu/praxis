package school.cesar.praxis.domain.prazo;

/** O que aconteceu com o prazo, na trilha gravada em {@code prazo_evento}. */
public enum TipoEventoPrazo {

    ABERTURA("Abertura"),
    SUSPENSAO("Suspensão"),
    RETOMADA("Retomada"),
    DILACAO("Dilação"),
    ALERTA("Alerta"),
    CUMPRIMENTO("Cumprimento"),
    CANCELAMENTO("Cancelamento"),
    REABERTURA("Reabertura");

    private final String rotulo;

    TipoEventoPrazo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
