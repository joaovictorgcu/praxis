package school.cesar.praxis.domain.prazo;

/**
 * Escalonamento do alerta de prazo. O nivel e derivado dos dias contaveis
 * restantes, e cada nivel e emitido no maximo uma vez por prazo.
 */
public enum NivelAlerta {

    ATENCAO(5),
    URGENTE(3),
    CRITICO(1),
    VENCE_HOJE(0);

    private final int diasRestantes;

    NivelAlerta(int diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public int diasRestantes() {
        return diasRestantes;
    }
}
