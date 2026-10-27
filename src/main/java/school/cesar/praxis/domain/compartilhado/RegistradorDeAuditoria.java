package school.cesar.praxis.domain.compartilhado;

/**
 * Porta de saida: grava o evento de auditoria. Implementada na
 * infraestrutura; o {@link ProxyDeAcesso} e os casos de uso de acesso so
 * enxergam este contrato, nunca o banco.
 */
public interface RegistradorDeAuditoria {

    void registrar(EventoDeAuditoria evento);
}
