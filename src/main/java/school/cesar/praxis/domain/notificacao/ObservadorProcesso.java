package school.cesar.praxis.domain.notificacao;

/** Observer: reage a eventos do agregado Processo. */
public interface ObservadorProcesso {

    void notificar(EventoProcesso evento);
}
