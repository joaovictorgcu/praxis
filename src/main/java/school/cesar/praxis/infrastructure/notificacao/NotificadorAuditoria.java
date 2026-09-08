package school.cesar.praxis.infrastructure.notificacao;

import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.notificacao.NotificadorDecorator;
import school.cesar.praxis.infrastructure.persistence.entity.NotificacaoEntity;
import school.cesar.praxis.infrastructure.persistence.repository.NotificacaoJpaRepository;

import java.time.LocalDateTime;

/**
 * Decorator concreto: grava a trilha de auditoria no banco. Perder prazo por
 * falta de aviso e responsabilidade civil do escritorio, entao o registro de
 * que o aviso saiu precisa ser duravel.
 */
public class NotificadorAuditoria extends NotificadorDecorator {

    private final NotificacaoJpaRepository repositorio;

    public NotificadorAuditoria(Notificador delegado, NotificacaoJpaRepository repositorio) {
        super(delegado);
        this.repositorio = repositorio;
    }

    @Override
    public void enviar(Notificacao notificacao) {
        super.enviar(notificacao);

        NotificacaoEntity registro = new NotificacaoEntity();
        registro.setDestinatario(notificacao.destinatario());
        registro.setAssunto(limitar(notificacao.assunto(), 300));
        registro.setCorpo(limitar(notificacao.corpo(), 1000));
        registro.setNumeroProcesso(notificacao.numeroProcesso());
        registro.setCanal("PAINEL+EMAIL");
        registro.setRegistradoEm(LocalDateTime.now());
        repositorio.save(registro);
    }

    private String limitar(String valor, int tamanho) {
        if (valor == null) {
            return "";
        }
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }
}
