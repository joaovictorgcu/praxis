package school.cesar.praxis.domain.prazo;

/**
 * Ciclo de vida do prazo. So {@link #EM_ABERTO} corre e entra na varredura;
 * {@link #SUSPENSO} esta congelado (CPC art. 221) e guarda o saldo que falta;
 * {@link #CUMPRIDO} e {@link #CANCELADO} sao encerramentos que admitem reabertura
 * justificada.
 *
 * <pre>
 *   EM_ABERTO --suspender--> SUSPENSO --retomar--> EM_ABERTO
 *   EM_ABERTO --dilatar----> EM_ABERTO (vencimento recalculado)
 *   EM_ABERTO --cumprir----> CUMPRIDO  --reabrir--> EM_ABERTO
 *   EM_ABERTO | SUSPENSO --cancelar--> CANCELADO --reabrir--> EM_ABERTO | SUSPENSO
 * </pre>
 */
public enum SituacaoPrazo {

    EM_ABERTO("em aberto"),
    SUSPENSO("suspenso"),
    CUMPRIDO("cumprido"),
    CANCELADO("cancelado");

    private final String rotulo;

    SituacaoPrazo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Cumprido ou cancelado: nao corre, nao alerta, so sai daqui por reabertura. */
    public boolean encerrado() {
        return this == CUMPRIDO || this == CANCELADO;
    }
}
