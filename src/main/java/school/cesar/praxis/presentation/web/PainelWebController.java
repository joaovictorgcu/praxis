package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Camada de apresentacao web (Thymeleaf): agenda de prazos, avisos do dia,
 * geracao de pecas e fluxo de aprovacao. Nao contem regra - so traduz
 * formulario em caso de uso.
 *
 * <p>A OAB do solicitante nao e mais digitada: vem do {@link UsuarioLogado}
 * da sessao. Assim o Proxy de segredo de justica confere quem realmente esta
 * logado, e nao uma OAB informada a mao.
 */
@Controller
@RequestMapping("/painel")
public class PainelWebController {

    private final PrazosUseCases.ConsultarAgenda agenda;
    private final PrazosUseCases.VarrerPrazos varredura;
    private final PrazosUseCases.CumprirPrazo cumprir;
    private final DocumentosUseCases.GerarDocumento gerar;
    private final DocumentosUseCases.ListarDocumentos listar;
    private final DocumentosUseCases.BaixarDocumento baixar;
    private final DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;
    private final DocumentosUseCases.AprovarDocumento aprovar;
    private final DocumentosUseCases.RejeitarDocumento rejeitar;
    private final DocumentosUseCases.DesfazerDecisaoDocumento desfazer;
    private final DocumentosUseCases.ProtocolarDocumento protocolar;
    private final DocumentosUseCases.HabilitarOab habilitarOab;
    private final ModelosUseCases.ListarModelos listarModelos;
    private final ProcessosUseCases.ListarProcessos listarProcessos;
    private final ClienteUseCase clientes;
    private final NotificadorPainel painel;

    /** Prefixo dos inputs gerados pela tela para os marcadores do modelo. */
    private static final String PREFIXO_CAMPO = "campo_";

    public PainelWebController(PrazosUseCases.ConsultarAgenda agenda,
                               PrazosUseCases.VarrerPrazos varredura,
                               PrazosUseCases.CumprirPrazo cumprir,
                               DocumentosUseCases.GerarDocumento gerar,
                               DocumentosUseCases.ListarDocumentos listar,
                               DocumentosUseCases.BaixarDocumento baixar,
                               DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao,
                               DocumentosUseCases.AprovarDocumento aprovar,
                               DocumentosUseCases.RejeitarDocumento rejeitar,
                               DocumentosUseCases.DesfazerDecisaoDocumento desfazer,
                               DocumentosUseCases.ProtocolarDocumento protocolar,
                               DocumentosUseCases.HabilitarOab habilitarOab,
                               ModelosUseCases.ListarModelos listarModelos,
                               ProcessosUseCases.ListarProcessos listarProcessos,
                               ClienteUseCase clientes,
                               NotificadorPainel painel) {
        this.agenda = agenda;
        this.varredura = varredura;
        this.cumprir = cumprir;
        this.gerar = gerar;
        this.listar = listar;
        this.baixar = baixar;
        this.enviarParaRevisao = enviarParaRevisao;
        this.aprovar = aprovar;
        this.rejeitar = rejeitar;
        this.desfazer = desfazer;
        this.protocolar = protocolar;
        this.habilitarOab = habilitarOab;
        this.listarModelos = listarModelos;
        this.listarProcessos = listarProcessos;
        this.clientes = clientes;
        this.painel = painel;
    }

    // --- Agenda de prazos ---

    @GetMapping
    public String agenda(@RequestParam(required = false) String ate, UsuarioLogado usuario, Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(30);
        if (ate != null && !ate.isBlank()) {
            try {
                limite = LocalDate.parse(ate);
            } catch (DateTimeParseException invalida) {
                model.addAttribute("erro", "data invalida: " + ate + " (use AAAA-MM-DD)");
            }
        }
        var itens = agenda.executar(limite);
        var documentos = listar.executar(null);

        model.addAttribute("hoje", hoje);
        model.addAttribute("limite", limite);
        model.addAttribute("itens", itens);
        model.addAttribute("avisos", painel.getEntregues());

        // Resumo do dia: o que exige acao de quem esta logado.
        model.addAttribute("vencidos", itens.stream().filter(PrazosUseCases.ConsultarAgenda.ItemAgenda::vencido).count());
        model.addAttribute("criticos", itens.stream()
                .filter(i -> !i.vencido() && i.diasRestantes() <= 3).count());
        model.addAttribute("meus", usuario == null ? 0 : itens.stream()
                .filter(i -> i.responsavel() != null && i.responsavel().equalsIgnoreCase(usuario.nome())).count());
        model.addAttribute("emRevisao", documentos.stream()
                .filter(d -> "EM_REVISAO".equals(d.status())).count());
        model.addAttribute("rascunhos", documentos.stream()
                .filter(d -> "RASCUNHO".equals(d.status()) || "REJEITADO".equals(d.status())).count());

        // Carteira do escritorio: os dois cartoes de contexto do dashboard.
        var carteira = listarProcessos.executar();
        model.addAttribute("processos", carteira.size());
        model.addAttribute("processosSigilosos", carteira.stream().filter(Processo::isSegredoJustica).count());
        model.addAttribute("clientesAtivos", clientes.listarClientes().size());
        model.addAttribute("clientesTotal", clientes.listarTodosOsClientes().size());
        return "painel";
    }

    @PostMapping("/varredura")
    public String varrer(@RequestParam(required = false) String hoje, RedirectAttributes flash) {
        int alertas = (hoje == null || hoje.isBlank()
                ? varredura.executarHoje()
                : varredura.executar(LocalDate.parse(hoje))).size();
        flash.addFlashAttribute("mensagem", alertas == 0
                ? "Varredura concluida: nenhum alerta novo."
                : "Varredura concluida: " + alertas + " alerta(s) emitido(s).");
        return "redirect:/painel";
    }

    @PostMapping("/prazos/{id}/cumprir")
    public String cumprirPrazo(@PathVariable Long id, RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel", "Prazo registrado como cumprido.",
                () -> cumprir.executar(id));
    }

    // --- Documentos ---

    @GetMapping("/documentos")
    public String documentos(@RequestParam(required = false) String processo, Model model) {
        model.addAttribute("processoFiltro", processo);
        model.addAttribute("documentos", listar.executar(processo));
        return comOpcoesDoFormulario(model);
    }

    @PostMapping("/documentos")
    public String gerarDocumento(@RequestParam String numeroProcesso,
                                 @RequestParam TipoDocumento tipo,
                                 @RequestParam(required = false) String codigoModelo,
                                 @RequestParam(required = false) String fatos,
                                 @RequestParam(required = false) String fundamentos,
                                 @RequestParam(required = false) String preliminares,
                                 @RequestParam(required = false) String merito,
                                 @RequestParam(required = false) String poderesEspeciais,
                                 @RequestParam(required = false) String camposLivres,
                                 @RequestParam Map<String, String> todosOsParametros,
                                 UsuarioLogado usuario,
                                 Model model) {
        Map<String, String> campos = new LinkedHashMap<>();
        adicionar(campos, "fatos", fatos);
        adicionar(campos, "fundamentos", fundamentos);
        adicionar(campos, "preliminares", preliminares);
        adicionar(campos, "merito", merito);
        adicionar(campos, "poderesEspeciais", poderesEspeciais);
        // Campos do modelo: um input por marcador, gerado na tela como campo_<nome>.
        todosOsParametros.forEach((nome, valor) -> {
            if (nome.startsWith(PREFIXO_CAMPO)) {
                adicionar(campos, nome.substring(PREFIXO_CAMPO.length()), valor);
            }
        });
        campos.putAll(lerCamposLivres(camposLivres)); // compatibilidade: "campo=valor" por linha

        model.addAttribute("processoFiltro", numeroProcesso);
        try {
            DocumentoGerado documento = gerar.executar(new DocumentosUseCases.GerarDocumento.Comando(
                    numeroProcesso, tipo, campos, usuario.oab(), vazioComoNulo(codigoModelo)));
            model.addAttribute("previa", documento.getConteudo());
            model.addAttribute("documentoGerado", documento.getId());
            model.addAttribute("mensagem", "Peca #" + documento.getId() + " gerada como rascunho.");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            model.addAttribute("erro", falha.getMessage());
        }

        model.addAttribute("documentos", listar.executar(numeroProcesso));
        return comOpcoesDoFormulario(model);
    }

    /** Leitura passa pelo Proxy com a OAB de quem esta logado. */
    @GetMapping("/documentos/{id}")
    public String verDocumento(@PathVariable Long id, UsuarioLogado usuario, Model model) {
        try {
            DocumentoGerado documento = baixar.executar(id, usuario.oab());
            model.addAttribute("previa", documento.getConteudo());
            model.addAttribute("documentoGerado", documento.getId());
            model.addAttribute("statusDocumento", documento.getStatus().nome());
            model.addAttribute("historico", documento.getHistorico());
            model.addAttribute("oabsHabilitadas", documento.getOabsHabilitadas());
            model.addAttribute("documentos", listar.executar(documento.getNumeroProcesso().valor()));
            model.addAttribute("processoFiltro", documento.getNumeroProcesso().valor());
        } catch (ProxyDeAcesso.AcessoNegadoException negado) {
            model.addAttribute("erro", negado.getMessage());
            model.addAttribute("documentos", listar.executar(null));
        } catch (NoSuchElementException | IllegalArgumentException falha) {
            model.addAttribute("erro", "documento nao encontrado: " + id);
            model.addAttribute("documentos", listar.executar(null));
        }
        return comOpcoesDoFormulario(model);
    }

    // --- Fluxo de aprovacao (State + Command) ---

    @PostMapping("/documentos/{id}/enviar-revisao")
    public String enviarParaRevisao(@PathVariable Long id, RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "Peca #" + id + " enviada para revisao do chefe.",
                () -> enviarParaRevisao.executar(new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(id)));
    }

    @SomenteChefe
    @PostMapping("/documentos/{id}/aprovar")
    public String aprovar(@PathVariable Long id,
                          @RequestParam(required = false) String comentario,
                          UsuarioLogado usuario,
                          RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "Peca #" + id + " aprovada.",
                () -> aprovar.executar(new DocumentosUseCases.AprovarDocumento.Comando(
                        id, usuario.oab(), vazioComoNulo(comentario) == null ? "de acordo" : comentario)));
    }

    @SomenteChefe
    @PostMapping("/documentos/{id}/rejeitar")
    public String rejeitar(@PathVariable Long id,
                           @RequestParam(required = false) String motivo,
                           UsuarioLogado usuario,
                           RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "Peca #" + id + " rejeitada.",
                () -> rejeitar.executar(new DocumentosUseCases.RejeitarDocumento.Comando(id, usuario.oab(), motivo)));
    }

    @SomenteChefe
    @PostMapping("/documentos/{id}/desfazer")
    public String desfazer(@PathVariable Long id, RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "Ultima decisao sobre a peca #" + id + " desfeita.",
                () -> desfazer.executar(new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(id)));
    }

    @PostMapping("/documentos/{id}/protocolar")
    public String protocolar(@PathVariable Long id, RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "Peca #" + id + " protocolada.",
                () -> protocolar.executar(new DocumentosUseCases.ProtocolarDocumento.Comando(id)));
    }

    /** Habilitar OAB em peca sigilosa: so o chefe amplia o acesso aos autos. */
    @SomenteChefe
    @PostMapping("/documentos/{id}/oabs")
    public String habilitarOab(@PathVariable Long id, @RequestParam String oab, RedirectAttributes flash) {
        return executarERedirecionar(flash, "/painel/documentos/" + id,
                "OAB " + oab + " habilitada na peca #" + id + ".",
                () -> habilitarOab.executar(new DocumentosUseCases.HabilitarOab.Comando(id, oab)));
    }

    // --- Apoio ---

    private String comOpcoesDoFormulario(Model model) {
        model.addAttribute("tipos", TipoDocumento.values());
        model.addAttribute("modelos", listarModelos.executar());
        return "documentos";
    }

    /**
     * Padrao POST-redirect-GET: a acao roda, o resultado vai como flash e a tela
     * e recarregada por GET - F5 nao repete a acao. Falha de dominio vira
     * mensagem na pagina, nunca 500.
     */
    private static String executarERedirecionar(RedirectAttributes flash, String destino,
                                                String sucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("mensagem", sucesso);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:" + destino;
    }

    private static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static void adicionar(Map<String, String> campos, String chave, String valor) {
        if (valor != null && !valor.isBlank()) {
            campos.put(chave, valor);
        }
    }

    /** Campos do modelo vem do formulario como "campo=valor", um por linha. */
    private static Map<String, String> lerCamposLivres(String texto) {
        Map<String, String> campos = new LinkedHashMap<>();
        if (texto == null || texto.isBlank()) {
            return campos;
        }
        for (String linha : texto.split("\\R")) {
            int separador = linha.indexOf('=');
            if (separador > 0) {
                adicionar(campos, linha.substring(0, separador).trim(),
                        linha.substring(separador + 1).trim());
            }
        }
        return campos;
    }
}
