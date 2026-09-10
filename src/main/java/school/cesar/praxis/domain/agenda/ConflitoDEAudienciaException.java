package school.cesar.praxis.domain.agenda;

/**
 * Exceção lançada quando há conflito de horário ao cadastrar uma audiência.
 * Regra de negócio: Duas audiências não podem estar marcadas para o mesmo horário na mesma sala.
 */
public class ConflitoDEAudienciaException extends RuntimeException {
    
    public ConflitoDEAudienciaException(String mensagem) {
        super(mensagem);
    }

    public ConflitoDEAudienciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
