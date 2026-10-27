package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.application.port.out.AuditoriaRepositorio;
import school.cesar.praxis.domain.compartilhado.EventoDeAuditoria;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;
import school.cesar.praxis.infrastructure.persistence.entity.AuditoriaAcessoEntity;
import school.cesar.praxis.infrastructure.persistence.repository.AuditoriaAcessoJpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador de saida da auditoria de acesso: grava (o Proxy e os casos de uso
 * de gestao de acesso so enxergam {@link RegistradorDeAuditoria}, nunca o
 * banco) e le, filtrada, para a tela de auditoria ({@link AuditoriaRepositorio}).
 */
@Repository
public class AuditoriaAcessoRepositorioJpa implements RegistradorDeAuditoria, AuditoriaRepositorio {

    private final AuditoriaAcessoJpaRepository jpa;

    public AuditoriaAcessoRepositorioJpa(AuditoriaAcessoJpaRepository jpa) {
        this.jpa = jpa;
    }

    /**
     * Transacao propria (REQUIRES_NEW): o registro de auditoria nao pode
     * desaparecer quando a leitura e negada e a transacao do caso de uso que a
     * pediu e revertida pela {@code AcessoNegadoException} - a tentativa
     * negada e exatamente o que mais importa ficar na trilha.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoDeAuditoria evento) {
        AuditoriaAcessoEntity entidade = new AuditoriaAcessoEntity();
        entidade.setTipoEvento(evento.tipoEvento());
        entidade.setConteudoTipo(evento.conteudoTipo());
        entidade.setConteudoId(evento.conteudoId());
        entidade.setNumeroProcesso(evento.numeroProcesso().valor());
        entidade.setOab(evento.oab());
        entidade.setUsuarioId(evento.usuarioId());
        entidade.setUsuarioNome(evento.usuarioNome());
        entidade.setQuando(evento.quando());
        entidade.setResultado(evento.resultado().name());
        entidade.setMotivo(evento.motivo());
        jpa.save(entidade);
    }

    @Override
    public List<AcessoUseCases.ItemAuditoria> consultar(String numeroProcesso, String oab,
                                                         ResultadoAcesso resultado, LocalDate de, LocalDate ate) {
        return jpa.findAllByOrderByQuandoDesc().stream()
                .filter(e -> numeroProcesso == null || numeroProcesso.isBlank() || e.getNumeroProcesso().equals(numeroProcesso))
                .filter(e -> oab == null || oab.isBlank() || oab.equals(e.getOab()))
                .filter(e -> resultado == null || resultado.name().equals(e.getResultado()))
                .filter(e -> de == null || !e.getQuando().toLocalDate().isBefore(de))
                .filter(e -> ate == null || !e.getQuando().toLocalDate().isAfter(ate))
                .map(AuditoriaAcessoRepositorioJpa::paraItem)
                .toList();
    }

    private static AcessoUseCases.ItemAuditoria paraItem(AuditoriaAcessoEntity e) {
        return new AcessoUseCases.ItemAuditoria(e.getTipoEvento(), e.getConteudoTipo(), e.getConteudoId(),
                e.getNumeroProcesso(), e.getOab(), e.getUsuarioId(), e.getUsuarioNome(), e.getQuando(),
                ResultadoAcesso.valueOf(e.getResultado()), e.getMotivo());
    }
}
