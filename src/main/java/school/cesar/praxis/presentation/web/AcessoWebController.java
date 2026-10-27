package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Camada de apresentacao do controle de acesso aos autos: acessos do
 * processo, solicitacoes e auditoria. Nao contem regra - so traduz
 * formulario em caso de uso, igual as demais telas do painel.
 */
@Controller
public class AcessoWebController {

    private final AcessoUseCases.ConcederAcesso conceder;
    private final AcessoUseCases.RevogarAcesso revogar;
    private final AcessoUseCases.ListarAcessosDoProcesso listarAcessos;
    private final AcessoUseCases.CriarSolicitacao criarSolicitacao;
    private final AcessoUseCases.CancelarSolicitacao cancelarSolicitacao;
    private final AcessoUseCases.AprovarSolicitacao aprovarSolicitacao;
    private final AcessoUseCases.NegarSolicitacao negarSolicitacao;
    private final AcessoUseCases.ListarSolicitacoes listarSolicitacoes;
    private final AcessoUseCases.ConsultarAuditoria consultarAuditoria;

    public AcessoWebController(AcessoUseCases.ConcederAcesso conceder,
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

    // --- Acessos do processo (so chefe) ---

    @SomenteChefe
    @GetMapping("/painel/processos/{numero}/acessos")
    public String acessosDoProcesso(@PathVariable String numero, Model model) {
        model.addAttribute("numeroProcesso", numero);
        model.addAttribute("acessos", listarAcessos.executar(numero));
        return "acessos-processo";
    }

    @SomenteChefe
    @PostMapping("/painel/processos/{numero}/acessos")
    public String conceder(@PathVariable String numero,
                           @RequestParam String oab,
                           @RequestParam(required = false) String expiraEm,
                           UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/processos/" + numero + "/acessos",
                "Acesso concedido à OAB " + oab + ".", () ->
                        conceder.executar(new AcessoUseCases.ConcederAcesso.Comando(
                                numero, oab, usuario.oab(), parseDataOpcional(expiraEm))));
    }

    @SomenteChefe
    @PostMapping("/painel/processos/{numero}/acessos/{id}/revogar")
    public String revogar(@PathVariable String numero, @PathVariable Long id,
                          @RequestParam String motivo, UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/processos/" + numero + "/acessos",
                "Acesso revogado.", () ->
                        revogar.executar(new AcessoUseCases.RevogarAcesso.Comando(id, motivo, usuario.oab())));
    }

    // --- Solicitacoes de acesso ---

    @GetMapping("/painel/solicitacoes")
    public String solicitacoes(UsuarioLogado usuario, Model model) {
        String filtro = usuario.chefe() ? null : usuario.oab();
        model.addAttribute("solicitacoes", listarSolicitacoes.executar(new AcessoUseCases.ListarSolicitacoes.Comando(filtro)));
        return "solicitacoes";
    }

    /** Disparada da ficha do processo; volta sempre para la. */
    @PostMapping("/painel/solicitacoes")
    public String solicitar(@RequestParam String numeroProcesso,
                            @RequestParam String justificativa,
                            UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/processos/" + numeroProcesso,
                "Solicitação de acesso enviada ao chefe.", () ->
                        criarSolicitacao.executar(new AcessoUseCases.CriarSolicitacao.Comando(
                                numeroProcesso, usuario.oab(), justificativa)));
    }

    @PostMapping("/painel/solicitacoes/{id}/cancelar")
    public String cancelar(@PathVariable Long id, UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/solicitacoes",
                "Solicitação cancelada.", () ->
                        cancelarSolicitacao.executar(new AcessoUseCases.CancelarSolicitacao.Comando(id, usuario.oab())));
    }

    @SomenteChefe
    @PostMapping("/painel/solicitacoes/{id}/aprovar")
    public String aprovar(@PathVariable Long id, @RequestParam(required = false) String expiraEm,
                          UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/solicitacoes",
                "Solicitação aprovada; acesso concedido.", () ->
                        aprovarSolicitacao.executar(new AcessoUseCases.AprovarSolicitacao.Comando(
                                id, usuario.oab(), parseDataOpcional(expiraEm))));
    }

    @SomenteChefe
    @PostMapping("/painel/solicitacoes/{id}/negar")
    public String negar(@PathVariable Long id, @RequestParam String motivo,
                        UsuarioLogado usuario, RedirectAttributes flash) {
        return executarRedirecionandoPara(flash, "/painel/solicitacoes",
                "Solicitação negada.", () ->
                        negarSolicitacao.executar(new AcessoUseCases.NegarSolicitacao.Comando(id, usuario.oab(), motivo)));
    }

    // --- Auditoria (so chefe) ---

    @SomenteChefe
    @GetMapping("/painel/auditoria")
    public String auditoria(@RequestParam(required = false) String processo,
                            @RequestParam(required = false) String oab,
                            @RequestParam(required = false) ResultadoAcesso resultado,
                            @RequestParam(required = false) String de,
                            @RequestParam(required = false) String ate,
                            Model model) {
        List<AcessoUseCases.ItemAuditoria> itens = consultarAuditoria.executar(new AcessoUseCases.ConsultarAuditoria.Filtro(
                processo, oab, resultado, parseDataOpcional(de), parseDataOpcional(ate)));
        model.addAttribute("itens", itens);
        model.addAttribute("processoFiltro", processo);
        model.addAttribute("oabFiltro", oab);
        model.addAttribute("resultadoFiltro", resultado);
        model.addAttribute("deFiltro", de);
        model.addAttribute("ateFiltro", ate);
        model.addAttribute("totalNegadas", itens.stream().filter(i -> i.resultado() == ResultadoAcesso.NEGADO).count());
        return "auditoria";
    }

    private static LocalDate parseDataOpcional(String data) {
        return data == null || data.isBlank() ? null : LocalDate.parse(data);
    }

    private static String executarRedirecionandoPara(RedirectAttributes flash, String destino,
                                                      String sucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("mensagem", sucesso);
        } catch (DateTimeParseException data) {
            flash.addFlashAttribute("erro", "data inválida: " + data.getParsedString());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:" + destino;
    }
}
