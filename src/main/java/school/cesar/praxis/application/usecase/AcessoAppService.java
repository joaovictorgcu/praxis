package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.application.port.in.AcessoUseCases.AprovarSolicitacao;
import school.cesar.praxis.application.port.in.AcessoUseCases.CancelarSolicitacao;
import school.cesar.praxis.application.port.in.AcessoUseCases.ConcederAcesso;
import school.cesar.praxis.application.port.in.AcessoUseCases.ConsultarAuditoria;
import school.cesar.praxis.application.port.in.AcessoUseCases.CriarSolicitacao;
import school.cesar.praxis.application.port.in.AcessoUseCases.ListarSolicitacoes;
import school.cesar.praxis.application.port.in.AcessoUseCases.NegarSolicitacao;
import school.cesar.praxis.application.port.in.AcessoUseCases.RevogarAcesso;
import school.cesar.praxis.application.port.out.AcessoAoProcessoRepositorio;
import school.cesar.praxis.application.port.out.AuditoriaRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.application.port.out.SolicitacaoAcessoRepositorio;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.acesso.SolicitacaoAcesso;
import school.cesar.praxis.domain.compartilhado.EventoDeAuditoria;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do controle de acesso aos autos: conceder/revogar acesso ao
 * processo, a maquina de estados da solicitacao e a consulta de auditoria.
 * A leitura em si (Proxy) nao passa por aqui - ver {@link BaixarDocumentoService}
 * e {@code AnexoAppService}.
 */
@Service
public class AcessoAppService implements AcessoUseCases.ConcederAcesso,
        AcessoUseCases.RevogarAcesso,
        AcessoUseCases.ListarAcessosDoProcesso,
        AcessoUseCases.CriarSolicitacao,
        AcessoUseCases.CancelarSolicitacao,
        AcessoUseCases.AprovarSolicitacao,
        AcessoUseCases.NegarSolicitacao,
        AcessoUseCases.ListarSolicitacoes,
        AcessoUseCases.ConsultarAuditoria {

    private final AcessoAoProcessoRepositorio acessos;
    private final SolicitacaoAcessoRepositorio solicitacoes;
    private final ProcessoRepositorio processos;
    private final UsuarioRepositorio usuarios;
    private final RegistradorDeAuditoria auditoria;
    private final AuditoriaRepositorio consultaAuditoria;
    private final Notificador notificador;
    private final Relogio relogio;

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AcessoAppService(AcessoAoProcessoRepositorio acessos,
                            SolicitacaoAcessoRepositorio solicitacoes,
                            ProcessoRepositorio processos,
                            UsuarioRepositorio usuarios,
                            RegistradorDeAuditoria auditoria,
                            AuditoriaRepositorio consultaAuditoria,
                            Notificador notificador,
                            Relogio relogio) {
        this.acessos = acessos;
        this.solicitacoes = solicitacoes;
        this.processos = processos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.consultaAuditoria = consultaAuditoria;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemAcesso executar(ConcederAcesso.Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException("processo não encontrado: " + comando.numeroProcesso()));

        LocalDate hoje = relogio.hoje();
        AcessoAoProcesso salvo = acessos.salvar(
                new AcessoAoProcesso(numero, comando.oab(), comando.concedidoPorOab(), hoje, comando.expiraEm()));

        auditoria.registrar(EventoDeAuditoria.gestao("CONCESSAO", numero, comando.oab(), null,
                comando.concedidoPorOab(), LocalDateTime.now(),
                comando.expiraEm() == null ? "acesso permanente concedido" : "acesso concedido até " + DATA_BR.format(comando.expiraEm())));

        return paraItem(salvo, hoje);
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemAcesso executar(RevogarAcesso.Comando comando) {
        AcessoAoProcesso alvo = acessos.porId(comando.acessoId())
                .orElseThrow(() -> new NoSuchElementException("acesso não encontrado: " + comando.acessoId()));

        LocalDate hoje = relogio.hoje();
        List<AcessoAoProcesso> doProcesso = acessos.porProcesso(alvo.getNumeroProcesso());
        if (!AcessoAoProcesso.podeRevogar(doProcesso, alvo.getId(), hoje)) {
            throw new IllegalStateException(
                    "processo sigiloso não pode ficar sem nenhum acesso ativo; conceda outro antes de revogar este");
        }

        alvo.revogar(hoje, comando.motivo());
        AcessoAoProcesso salvo = acessos.salvar(alvo);

        auditoria.registrar(EventoDeAuditoria.gestao("REVOGACAO", alvo.getNumeroProcesso(), alvo.getOab(), null,
                comando.revogadoPorOab(), LocalDateTime.now(), comando.motivo()));

        return paraItem(salvo, hoje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcessoUseCases.ItemAcesso> executar(String numeroProcesso) {
        LocalDate hoje = relogio.hoje();
        return acessos.porProcesso(NumeroCnj.de(numeroProcesso)).stream()
                .map(a -> paraItem(a, hoje))
                .sorted((a, b) -> b.concedidoEm().compareTo(a.concedidoEm()))
                .toList();
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemSolicitacao executar(CriarSolicitacao.Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException("processo não encontrado: " + comando.numeroProcesso()));
        if (!processo.isSegredoJustica()) {
            throw new IllegalArgumentException(
                    "processo não está em segredo de justiça; não é preciso solicitar acesso");
        }

        LocalDate hoje = relogio.hoje();
        boolean jaTemAcesso = acessos.porProcesso(numero).stream()
                .anyMatch(a -> a.getOab().equals(comando.oabSolicitante()) && a.ativo(hoje));
        if (jaTemAcesso) {
            throw new IllegalArgumentException("esta OAB já tem acesso ativo a este processo");
        }

        List<SolicitacaoAcesso> doProcesso = solicitacoes.porProcesso(numero);
        if (SolicitacaoAcesso.existePendenteDaMesmaOab(doProcesso, comando.oabSolicitante())) {
            throw new IllegalArgumentException("já existe uma solicitação pendente desta OAB para este processo");
        }

        SolicitacaoAcesso salva = solicitacoes.salvar(
                new SolicitacaoAcesso(numero, comando.oabSolicitante(), comando.justificativa(), hoje));

        auditoria.registrar(EventoDeAuditoria.gestao("SOLICITACAO_CRIADA", numero, comando.oabSolicitante(),
                null, null, LocalDateTime.now(), comando.justificativa()));
        notificarChefesNovaSolicitacao(salva);

        return paraItem(salva);
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemSolicitacao executar(CancelarSolicitacao.Comando comando) {
        SolicitacaoAcesso solicitacao = carregarSolicitacao(comando.solicitacaoId());
        if (!solicitacao.getOabSolicitante().equals(comando.oabSolicitante())) {
            throw new IllegalArgumentException("só quem solicitou pode cancelar a própria solicitação");
        }
        solicitacao.cancelar(relogio.hoje());
        return paraItem(solicitacoes.salvar(solicitacao));
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemSolicitacao executar(AprovarSolicitacao.Comando comando) {
        SolicitacaoAcesso solicitacao = carregarSolicitacao(comando.solicitacaoId());
        LocalDate hoje = relogio.hoje();
        solicitacao.aprovar(comando.oabChefe(), hoje);
        SolicitacaoAcesso salva = solicitacoes.salvar(solicitacao);

        acessos.salvar(new AcessoAoProcesso(solicitacao.getNumeroProcesso(), solicitacao.getOabSolicitante(),
                comando.oabChefe(), hoje, comando.expiraEm()));

        auditoria.registrar(EventoDeAuditoria.gestao("SOLICITACAO_APROVADA", solicitacao.getNumeroProcesso(),
                solicitacao.getOabSolicitante(), null, comando.oabChefe(), LocalDateTime.now(),
                comando.expiraEm() == null ? "acesso permanente concedido" : "acesso concedido até " + DATA_BR.format(comando.expiraEm())));
        notificarSolicitante(solicitacao, true, null);

        return paraItem(salva);
    }

    @Override
    @Transactional
    public AcessoUseCases.ItemSolicitacao executar(NegarSolicitacao.Comando comando) {
        SolicitacaoAcesso solicitacao = carregarSolicitacao(comando.solicitacaoId());
        solicitacao.negar(comando.oabChefe(), comando.motivo(), relogio.hoje());
        SolicitacaoAcesso salva = solicitacoes.salvar(solicitacao);

        auditoria.registrar(EventoDeAuditoria.gestao("SOLICITACAO_NEGADA", solicitacao.getNumeroProcesso(),
                solicitacao.getOabSolicitante(), null, comando.oabChefe(), LocalDateTime.now(), comando.motivo()));
        notificarSolicitante(solicitacao, false, comando.motivo());

        return paraItem(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcessoUseCases.ItemSolicitacao> executar(ListarSolicitacoes.Comando comando) {
        String oabFiltro = comando.oabFiltro();
        List<SolicitacaoAcesso> encontradas = oabFiltro == null || oabFiltro.isBlank()
                ? solicitacoes.listar()
                : solicitacoes.porOabSolicitante(oabFiltro);
        return encontradas.stream()
                .map(AcessoAppService::paraItem)
                .sorted((a, b) -> b.solicitadoEm().compareTo(a.solicitadoEm()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcessoUseCases.ItemAuditoria> executar(ConsultarAuditoria.Filtro filtro) {
        return consultaAuditoria.consultar(
                        filtro.numeroProcesso(), filtro.oab(), filtro.resultado(), filtro.de(), filtro.ate())
                .stream()
                // A coluna Usuario sempre mostra o nome resolvido pela OAB, nunca a OAB
                // nem "desconhecido": o evento pode ter sido gravado sem usuarioId/nome
                // (chamada direta, fora da sessao web), mas a OAB envolvida sempre tem.
                .map(i -> new AcessoUseCases.ItemAuditoria(i.tipoEvento(), i.conteudoTipo(), i.conteudoId(),
                        i.numeroProcesso(), i.oab(), i.usuarioId(), nomeDoUsuario(i.oab()), i.quando(),
                        i.resultado(), i.motivo()))
                .toList();
    }

    /** Nome de quem tem esta OAB, buscado no cadastro de usuarios; sem cadastro, a propria OAB (nunca "desconhecido"). */
    private String nomeDoUsuario(String oab) {
        if (oab == null || oab.isBlank()) {
            return oab;
        }
        return usuarios.porOab(oab).map(Usuario::getNome).orElse(oab);
    }

    private SolicitacaoAcesso carregarSolicitacao(Long id) {
        return solicitacoes.porId(id)
                .orElseThrow(() -> new NoSuchElementException("solicitação não encontrada: " + id));
    }

    private void notificarChefesNovaSolicitacao(SolicitacaoAcesso solicitacao) {
        String numero = solicitacao.getNumeroProcesso().valor();
        usuarios.listar().stream()
                .filter(u -> u.getPapel() == Papel.CHEFE)
                .forEach(chefe -> notificador.enviar(new Notificacao(chefe.getEmail(),
                        "Nova solicitação de acesso - processo " + numero,
                        "A OAB " + solicitacao.getOabSolicitante() + " solicitou acesso ao processo " + numero
                                + ". Justificativa: " + solicitacao.getJustificativa(),
                        numero)));
    }

    /**
     * O email vem do Usuario (quem loga e solicita), nao do cadastro de
     * Advogado do escritorio: sao agregados diferentes, e so o Usuario e
     * garantido existir para quem conseguiu solicitar - precisou estar logado.
     */
    private void notificarSolicitante(SolicitacaoAcesso solicitacao, boolean aprovada, String motivoNegacao) {
        String numero = solicitacao.getNumeroProcesso().valor();
        usuarios.porOab(solicitacao.getOabSolicitante()).ifPresent(usuario -> notificador.enviar(new Notificacao(
                usuario.getEmail(),
                (aprovada ? "Acesso aprovado" : "Acesso negado") + " - processo " + numero,
                aprovada
                        ? "Sua solicitação de acesso ao processo " + numero + " foi aprovada."
                        : "Sua solicitação de acesso ao processo " + numero + " foi negada. Motivo: " + motivoNegacao,
                numero)));
    }

    private AcessoUseCases.ItemAcesso paraItem(AcessoAoProcesso a, LocalDate hoje) {
        return new AcessoUseCases.ItemAcesso(a.getId(), a.getNumeroProcesso().valor(), a.getOab(),
                nomeDoUsuario(a.getConcedidoPor()), a.getConcedidoEm(), a.getExpiraEm(), a.getRevogadoEm(),
                a.getMotivoRevogacao(), a.ativo(hoje), a.expirado(hoje), a.diasParaExpirar(hoje));
    }

    private static AcessoUseCases.ItemSolicitacao paraItem(SolicitacaoAcesso s) {
        return new AcessoUseCases.ItemSolicitacao(s.getId(), s.getNumeroProcesso().valor(), s.getOabSolicitante(),
                s.getJustificativa(), s.getSolicitadoEm(), s.getStatus().name(), s.getDecididoEm(),
                s.getDecididoPorOab(), s.getMotivoNegacao());
    }
}
