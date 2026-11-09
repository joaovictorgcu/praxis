package school.cesar.praxis.presentation.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.domain.prazo.OrigemVarredura;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.PrerrogativaPrazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de apresentacao (REST) da funcionalidade Motor de prazos com alertas.
 *
 * <p>Quem age vem da sessao (o interceptor da API exige login para tudo que
 * muda estado) e vai para a trilha do prazo; nunca do corpo da requisicao.
 */
@RestController
@RequestMapping("/api/prazos")
public class PrazoRestController {

    private final PrazosUseCases.AbrirPrazo abrirPrazo;
    private final PrazosUseCases.CumprirPrazo cumprirPrazo;
    private final PrazosUseCases.VarrerPrazos varrerPrazos;
    private final PrazosUseCases.ConsultarAgenda consultarAgenda;
    private final PrazosUseCases.ConsultarPrazo consultarPrazo;
    private final PrazosUseCases.SuspenderPrazo suspenderPrazo;
    private final PrazosUseCases.RetomarPrazo retomarPrazo;
    private final PrazosUseCases.DilatarPrazo dilatarPrazo;
    private final PrazosUseCases.CancelarPrazo cancelarPrazo;
    private final PrazosUseCases.ReabrirPrazo reabrirPrazo;
    private final PrazosUseCases.ListarVarreduras listarVarreduras;

    public PrazoRestController(PrazosUseCases.AbrirPrazo abrirPrazo,
                               PrazosUseCases.CumprirPrazo cumprirPrazo,
                               PrazosUseCases.VarrerPrazos varrerPrazos,
                               PrazosUseCases.ConsultarAgenda consultarAgenda,
                               PrazosUseCases.ConsultarPrazo consultarPrazo,
                               PrazosUseCases.SuspenderPrazo suspenderPrazo,
                               PrazosUseCases.RetomarPrazo retomarPrazo,
                               PrazosUseCases.DilatarPrazo dilatarPrazo,
                               PrazosUseCases.CancelarPrazo cancelarPrazo,
                               PrazosUseCases.ReabrirPrazo reabrirPrazo,
                               PrazosUseCases.ListarVarreduras listarVarreduras) {
        this.abrirPrazo = abrirPrazo;
        this.cumprirPrazo = cumprirPrazo;
        this.varrerPrazos = varrerPrazos;
        this.consultarAgenda = consultarAgenda;
        this.consultarPrazo = consultarPrazo;
        this.suspenderPrazo = suspenderPrazo;
        this.retomarPrazo = retomarPrazo;
        this.dilatarPrazo = dilatarPrazo;
        this.cancelarPrazo = cancelarPrazo;
        this.reabrirPrazo = reabrirPrazo;
        this.listarVarreduras = listarVarreduras;
    }

    /** {@code prerrogativa} e opcional: sem ela, prazo simples. */
    public record NovoPrazo(@NotBlank String numeroProcesso,
                            @NotBlank String descricao,
                            LocalDate intimacao,
                            @Positive int quantidadeDias,
                            boolean fatal,
                            RegimeContagem regime,
                            PrerrogativaPrazo prerrogativa) {
    }

    public record Suspensao(LocalDate inicio, String motivo) {
    }

    public record Retomada(LocalDate retomada) {
    }

    public record Dilacao(int dias, String fundamento) {
    }

    public record Motivo(String motivo) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> abrir(@Valid @RequestBody NovoPrazo corpo, UsuarioLogado usuario) {
        Prazo prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                corpo.numeroProcesso(),
                corpo.descricao(),
                corpo.intimacao(),
                corpo.quantidadeDias(),
                corpo.fatal(),
                corpo.regime() == null ? RegimeContagem.DIAS_UTEIS : corpo.regime(),
                corpo.prerrogativa() == null ? PrerrogativaPrazo.NENHUMA : corpo.prerrogativa(),
                autor(usuario)));
        return ResponseEntity.ok(resumo(prazo));
    }

    @GetMapping("/{id}")
    public PrazosUseCases.ConsultarPrazo.DetalhePrazo detalhe(@PathVariable Long id) {
        return consultarPrazo.executar(id);
    }

    @PostMapping("/{id}/cumprir")
    public Map<String, Object> cumprir(@PathVariable Long id, UsuarioLogado usuario) {
        Prazo prazo = cumprirPrazo.executar(id, autor(usuario));
        Map<String, Object> resposta = resumo(prazo);
        resposta.put("cumprido", prazo.isCumprido());
        resposta.put("cumpridoEm", String.valueOf(prazo.getCumpridoEm()));
        return resposta;
    }

    @PostMapping("/{id}/suspender")
    public Map<String, Object> suspender(@PathVariable Long id, @RequestBody Suspensao corpo, UsuarioLogado usuario) {
        return resumo(suspenderPrazo.executar(new PrazosUseCases.SuspenderPrazo.Comando(
                id, corpo.inicio(), corpo.motivo(), autor(usuario))));
    }

    @PostMapping("/{id}/retomar")
    public Map<String, Object> retomar(@PathVariable Long id, @RequestBody Retomada corpo, UsuarioLogado usuario) {
        return resumo(retomarPrazo.executar(new PrazosUseCases.RetomarPrazo.Comando(
                id, corpo.retomada(), autor(usuario))));
    }

    @PostMapping("/{id}/dilatar")
    public Map<String, Object> dilatar(@PathVariable Long id, @RequestBody Dilacao corpo, UsuarioLogado usuario) {
        return resumo(dilatarPrazo.executar(new PrazosUseCases.DilatarPrazo.Comando(
                id, corpo.dias(), corpo.fundamento(), autor(usuario))));
    }

    @PostMapping("/{id}/cancelar")
    public Map<String, Object> cancelar(@PathVariable Long id, @RequestBody Motivo corpo, UsuarioLogado usuario) {
        return resumo(cancelarPrazo.executar(new PrazosUseCases.CancelarPrazo.Comando(
                id, corpo.motivo(), autor(usuario))));
    }

    @PostMapping("/{id}/reabrir")
    public Map<String, Object> reabrir(@PathVariable Long id, @RequestBody Motivo corpo, UsuarioLogado usuario) {
        return resumo(reabrirPrazo.executar(new PrazosUseCases.ReabrirPrazo.Comando(
                id, corpo.motivo(), autor(usuario))));
    }

    @GetMapping("/agenda")
    public List<PrazosUseCases.ConsultarAgenda.ItemAgenda> agenda(
            @RequestParam(required = false) String ate) {
        LocalDate limite = ate == null ? LocalDate.now().plusDays(30) : LocalDate.parse(ate);
        return consultarAgenda.executar(limite);
    }

    /** Varredura manual - o job diario chama o mesmo caso de uso, com origem automatica. */
    @PostMapping("/varredura")
    public Map<String, Object> varrer(@RequestParam(required = false) String hoje, UsuarioLogado usuario) {
        PrazosUseCases.VarrerPrazos.Resultado resultado = varrerPrazos.executar(new PrazosUseCases.VarrerPrazos.Comando(
                hoje == null ? LocalDate.now() : LocalDate.parse(hoje),
                OrigemVarredura.MANUAL,
                usuario == null ? null : usuario.nome()));
        List<AlertaPrazo> alertas = resultado.alertas();
        return Map.of(
                "execucao", resultado.execucao().id(),
                "prazosAvaliados", resultado.execucao().prazosAvaliados(),
                "alertasEmitidos", alertas.size(),
                "alertas", alertas.stream()
                        .map(alerta -> Map.of(
                                "processo", alerta.numeroProcesso(),
                                "prazo", alerta.descricaoPrazo(),
                                "nivel", alerta.nivel().name(),
                                "diasRestantes", alerta.diasRestantes(),
                                "vencimento", alerta.vencimento().toString(),
                                "destinatario", alerta.destinatario().email()))
                        .toList());
    }

    /** Historico das execucoes gravado em {@code varredura_execucao}. */
    @GetMapping("/varreduras")
    public List<ExecucaoVarredura> varreduras(@RequestParam(defaultValue = "10") int limite) {
        return listarVarreduras.executar(limite);
    }

    private static PrazosUseCases.Autor autor(UsuarioLogado usuario) {
        return usuario == null
                ? PrazosUseCases.Autor.sistema()
                : new PrazosUseCases.Autor(usuario.oab(), usuario.nome(), usuario.chefe());
    }

    private static Map<String, Object> resumo(Prazo prazo) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", prazo.getId());
        resposta.put("processo", prazo.getNumeroProcesso().valor());
        resposta.put("descricao", prazo.getDescricao());
        resposta.put("regime", prazo.getRegime().name());
        resposta.put("prerrogativa", prazo.getPrerrogativa().name());
        resposta.put("diasEfetivos", prazo.diasEfetivos());
        resposta.put("situacao", prazo.getSituacao().name());
        resposta.put("vencimentoOriginal", prazo.getVencimentoOriginal().toString());
        resposta.put("vencimento", prazo.getVencimento().toString());
        resposta.put("fatal", prazo.isFatal());
        return resposta;
    }
}
