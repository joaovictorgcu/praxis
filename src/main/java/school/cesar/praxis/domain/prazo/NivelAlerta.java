package school.cesar.praxis.domain.prazo;

/**
 * Escalonamento do alerta de prazo. O nivel e derivado dos dias contaveis
 * restantes, e cada nivel e emitido no maximo uma vez por prazo.
 */
public enum NivelAlerta {

    ATENCAO(5, "ATENÇÃO"),
    URGENTE(3, "URGENTE"),
    CRITICO(1, "CRÍTICO"),
    VENCE_HOJE(0, "VENCE HOJE"),
    /** Prazo em aberto apos o vencimento: aviso de perda, distinto do "vence hoje". */
    VENCIDO(-1, "VENCIDO");

    private final int diasRestantes;
    private final String rotulo;

    NivelAlerta(int diasRestantes, String rotulo) {
        this.diasRestantes = diasRestantes;
        this.rotulo = rotulo;
    }

    /** Texto de tela e da trilha do prazo; a regra continua comparando a constante. */
    public String rotulo() {
        return rotulo;
    }

    public int diasRestantes() {
        return diasRestantes;
    }
}
