package school.cesar.praxis.domain.agenda;

/**
 * Exceção lançada quando há erro na definição dos horários de uma audiência.
 * Exemplo: Horário final antes do horário inicial.
 */
public class HorarioInvalidoException extends RuntimeException {
    
    public HorarioInvalidoException(String mensagem) {
        super(mensagem);
    }

    public HorarioInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
