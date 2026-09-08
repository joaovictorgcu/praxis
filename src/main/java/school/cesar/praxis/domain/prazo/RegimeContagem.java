package school.cesar.praxis.domain.prazo;

/**
 * Regime legal de contagem. Prazos processuais correm em dias uteis
 * (art. 219 do CPC); prazos materiais e de leis especiais, em dias corridos.
 */
public enum RegimeContagem {
    DIAS_UTEIS,
    DIAS_CORRIDOS
}
