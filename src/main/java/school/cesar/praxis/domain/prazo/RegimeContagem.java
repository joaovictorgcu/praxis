package school.cesar.praxis.domain.prazo;

/**
 * Regime legal de contagem. Prazos processuais correm em dias uteis
 * (art. 219 do CPC); prazos materiais e de leis especiais, em dias corridos.
 */
public enum RegimeContagem {
    DIAS_UTEIS,
    DIAS_CORRIDOS;

    /** "1 dia útil", "4 dias úteis", "10 dias corridos": texto da trilha do prazo. */
    public String descrever(int dias) {
        if (this == DIAS_UTEIS) {
            return dias == 1 ? "1 dia útil" : dias + " dias úteis";
        }
        return dias == 1 ? "1 dia corrido" : dias + " dias corridos";
    }
}
