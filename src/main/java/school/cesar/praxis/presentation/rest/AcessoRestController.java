package school.cesar.praxis.presentation.rest;

import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.time.LocalDate;
import java.util.List;

/**
 * Camada de apresentacao (REST) do controle de acesso aos autos: concessao e
 * revogacao de acesso ao processo, solicitacao de acesso e auditoria. Mesmas
 * regras de papel das telas - conceder, revogar, aprovar, negar e consultar
 * auditoria sao do chefe; solicitar e cancelar a propria solicitacao, de
 * qualquer advogado logado.
 */
@RestController
public class AcessoRestController {

    private final AcessoUseCases.ConcederAcesso conceder;
    private final AcessoUseCases.RevogarAcesso revogar;
    private final AcessoUseCases.ListarAcessosDoProcesso listarAcessos;
    private final AcessoUseCases.CriarSolicitacao criarSolicitacao;
    private final AcessoUseCases.CancelarSolicitacao cancelarSolicitacao;
    private final AcessoUseCases.AprovarSolicitacao aprovarSolicitacao;
    private final AcessoUseCases.NegarSolicitacao negarSolicitacao;
    private final AcessoUseCases.ListarSolicitacoes listarSolicitacoes;
    private final AcessoUseCases.ConsultarAuditoria consultarAuditoria;

    public AcessoRestController(AcessoUseCases.ConcederAcesso conceder,
                                AcessoUseCases.RevogarAcesso revogar,
                                AcessoUseCases.ListarAcessosDoProcesso listarAcessos,
                                AcessoUseCases.CriarSolicitacao criarSolicitacao,
                                AcessoUseCases.CancelarSolicitacao cancelarSolicitacao,
                                AcessoUseCases.AprovarSolicitacao aprovarSolicitacao,
                                AcessoUseCases.NegarSolicitacao negarSolicitacao,
                                AcessoUseCases.ListarSolicitacoes listarSolicitacoes,
                                AcessoUseCases.ConsultarAuditoria consultarAuditoria) {
        this.conceder = conceder;
        this.revogar = revogar;
        this.listarAcessos = listarAcessos;
        this.criarSolicitacao = criarSolicitacao;
        this.cancelarSolicitacao = cancelarSolicitacao;
        this.aprovarSolicitacao = aprovarSolicitacao;
        this.negarSolicitacao = negarSolicitacao;
        this.listarSolicitacoes = listarSolicitacoes;
        this.consultarAuditoria = consultarAuditoria;
    }

    public record NovoAcesso(String numeroProcesso, String oab, LocalDate expiraEm) {
    }

    public record Revogacao(String motivo) {
    }

    public record NovaSolicitacao(String numeroProcesso, String justificativa) {
    }

    public record Decisao(LocalDate expiraEm, String motivo) {
    }

    @SomenteChefe
    @PostMapping("/api/acessos")
    public AcessoUseCases.ItemAcesso conceder(@RequestBody NovoAcesso corpo, UsuarioLogado usuario) {
        return conceder.executar(new AcessoUseCases.ConcederAcesso.Comando(
                corpo.numeroProcesso(), corpo.oab(), usuario.oab(), corpo.expiraEm()));
    }

    @SomenteChefe
    @GetMapping("/api/acessos")
    public List<AcessoUseCases.ItemAcesso> listar(@RequestParam String processo) {
        return listarAcessos.executar(processo);
    }

    @SomenteChefe
    @PostMapping("/api/acessos/{id}/revogar")
    public AcessoUseCases.ItemAcesso revogar(@PathVariable Long id, @RequestBody Revogacao corpo,
                                             UsuarioLogado usuario) {
        return revogar.executar(new AcessoUseCases.RevogarAcesso.Comando(id, corpo.motivo(), usuario.oab()));
    }

    @PostMapping("/api/solicitacoes")
    public AcessoUseCases.ItemSolicitacao solicitar(@RequestBody NovaSolicitacao corpo, UsuarioLogado usuario) {
        return criarSolicitacao.executar(new AcessoUseCases.CriarSolicitacao.Comando(
                corpo.numeroProcesso(), usuario.oab(), corpo.justificativa()));
    }

    @PostMapping("/api/solicitacoes/{id}/cancelar")
    public AcessoUseCases.ItemSolicitacao cancelar(@PathVariable Long id, UsuarioLogado usuario) {
        return cancelarSolicitacao.executar(new AcessoUseCases.CancelarSolicitacao.Comando(id, usuario.oab()));
    }

    @SomenteChefe
    @PostMapping("/api/solicitacoes/{id}/aprovar")
    public AcessoUseCases.ItemSolicitacao aprovar(@PathVariable Long id, @RequestBody Decisao corpo,
                                                  UsuarioLogado usuario) {
        return aprovarSolicitacao.executar(new AcessoUseCases.AprovarSolicitacao.Comando(
                id, usuario.oab(), corpo.expiraEm()));
    }

    @SomenteChefe
    @PostMapping("/api/solicitacoes/{id}/negar")
    public AcessoUseCases.ItemSolicitacao negar(@PathVariable Long id, @RequestBody Decisao corpo,
                                                UsuarioLogado usuario) {
        return negarSolicitacao.executar(new AcessoUseCases.NegarSolicitacao.Comando(id, usuario.oab(), corpo.motivo()));
    }

    /** Chefe ve a fila inteira; advogado, so as proprias solicitacoes. */
    @GetMapping("/api/solicitacoes")
    public List<AcessoUseCases.ItemSolicitacao> listarSolicitacoes(UsuarioLogado usuario) {
        String filtro = usuario.chefe() ? null : usuario.oab();
        return listarSolicitacoes.executar(new AcessoUseCases.ListarSolicitacoes.Comando(filtro));
    }

    @SomenteChefe
    @GetMapping("/api/auditoria")
    public List<AcessoUseCases.ItemAuditoria> auditoria(@RequestParam(required = false) String processo,
                                                         @RequestParam(required = false) String oab,
                                                         @RequestParam(required = false) ResultadoAcesso resultado,
                                                         @RequestParam(required = false) LocalDate de,
                                                         @RequestParam(required = false) LocalDate ate) {
        return consultarAuditoria.executar(new AcessoUseCases.ConsultarAuditoria.Filtro(processo, oab, resultado, de, ate));
    }
}
