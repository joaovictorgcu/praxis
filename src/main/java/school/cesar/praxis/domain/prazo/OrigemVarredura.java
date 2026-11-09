package school.cesar.praxis.domain.prazo;

/** Quem disparou a varredura: o agendador diario ou alguem pelo painel/API. */
public enum OrigemVarredura {

    AUTOMATICA("automática"),
    MANUAL("manual");

    private final String rotulo;

    OrigemVarredura(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
