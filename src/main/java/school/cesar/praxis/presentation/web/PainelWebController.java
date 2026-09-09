package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Camada de apresentacao web (Thymeleaf): agenda de prazos, avisos do dia e
 * geracao de pecas. Nao contem regra - so traduz formulario em caso de uso.
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
    private final ModelosUseCases.ListarModelos listarModelos;
    private final NotificadorPainel painel;

    public PainelWebController(PrazosUseCases.ConsultarAgenda agenda,
                               PrazosUseCases.VarrerPrazos varredura,
                               PrazosUseCases.CumprirPrazo cumprir,
                               DocumentosUseCases.GerarDocumento gerar,
                               DocumentosUseCases.ListarDocumentos listar,
                               DocumentosUseCases.BaixarDocumento baixar,
                               ModelosUseCases.ListarModelos listarModelos,
                               NotificadorPainel painel) {
        this.agenda = agenda;
        this.varredura = varredura;
        this.cumprir = cumprir;
        this.gerar = gerar;
        this.listar = listar;
        this.baixar = baixar;
        this.listarModelos = listarModelos;
        this.painel = painel;
    }

    @GetMapping
    public String agenda(@RequestParam(required = false) String ate, Model model) {
        LocalDate limite = ate == null ? LocalDate.now().plusDays(30) : LocalDate.parse(ate);
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("limite", limite);
        model.addAttribute("itens", agenda.executar(limite));
        model.addAttribute("avisos", painel.getEntregues());
        return "painel";
    }

    @PostMapping("/varredura")
    public String varrer(@RequestParam(required = false) String hoje) {
        if (hoje == null || hoje.isBlank()) {
            varredura.executarHoje();
        } else {
            varredura.executar(LocalDate.parse(hoje));
        }
        return "redirect:/painel";
    }

    @PostMapping("/prazos/{id}/cumprir")
    public String cumprirPrazo(@PathVariable Long id) {
        cumprir.executar(id);
        return "redirect:/painel";
    }

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
                                 @RequestParam(required = false) String poderesEspeciais,
                                 @RequestParam(required = false) String camposLivres,
                                 @RequestParam(required = false) String oab,
                                 Model model) {
        Map<String, String> campos = new LinkedHashMap<>();
        adicionar(campos, "fatos", fatos);
        adicionar(campos, "fundamentos", fundamentos);
        adicionar(campos, "poderesEspeciais", poderesEspeciais);
        campos.putAll(lerCamposLivres(camposLivres));

        model.addAttribute("processoFiltro", numeroProcesso);
        try {
            DocumentoGerado documento = gerar.executar(new DocumentosUseCases.GerarDocumento.Comando(
                    numeroProcesso, tipo, campos, oab, codigoModelo));
            model.addAttribute("previa", documento.getConteudo());
            model.addAttribute("documentoGerado", documento.getId());
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            model.addAttribute("erro", falha.getMessage());
        }

        model.addAttribute("documentos", listar.executar(numeroProcesso));
        return comOpcoesDoFormulario(model);
    }

    @GetMapping("/documentos/{id}")
    public String verDocumento(@PathVariable Long id,
                               @RequestParam String oab,
                               Model model) {
        try {
            DocumentoGerado documento = baixar.executar(id, oab);
            model.addAttribute("previa", documento.getConteudo());
            model.addAttribute("documentos", listar.executar(documento.getNumeroProcesso().valor()));
            model.addAttribute("processoFiltro", documento.getNumeroProcesso().valor());
        } catch (ProxyDeAcesso.AcessoNegadoException negado) {
            model.addAttribute("erro", negado.getMessage());
            model.addAttribute("documentos", listar.executar(null));
        }
        return comOpcoesDoFormulario(model);
    }

    private String comOpcoesDoFormulario(Model model) {
        model.addAttribute("tipos", TipoDocumento.values());
        model.addAttribute("modelos", listarModelos.executar());
        return "documentos";
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
