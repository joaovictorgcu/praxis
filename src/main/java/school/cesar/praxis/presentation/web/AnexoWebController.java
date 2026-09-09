package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.anexo.TipoArquivo;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Camada de apresentacao web da juntada de arquivos. Nao contem regra - so
 * traduz formulario em caso de uso e mostra a recusa.
 *
 * <p>O download nao passa por aqui: a tela aponta para {@code /api/anexos/{id}},
 * porque anexo e binario e precisa sair como arquivo, nao como pagina. A recusa
 * por segredo de justica chega ao navegador como 403, vinda do Proxy.
 */
@Controller
@RequestMapping("/painel/anexos")
public class AnexoWebController {

    private final AnexosUseCases.AnexarArquivo anexar;
    private final AnexosUseCases.ListarAnexos listar;

    public AnexoWebController(AnexosUseCases.AnexarArquivo anexar,
                              AnexosUseCases.ListarAnexos listar) {
        this.anexar = anexar;
        this.listar = listar;
    }

    @GetMapping
    public String anexos(@RequestParam(required = false) String processo, Model model) {
        model.addAttribute("processoFiltro", processo);
        return montarTela(model, processo);
    }

    @PostMapping
    public String anexar(@RequestParam String numeroProcesso,
                         @RequestParam MultipartFile arquivo,
                         @RequestParam(required = false) String descricao,
                         @RequestParam(required = false) String oab,
                         Model model) throws IOException {
        model.addAttribute("processoFiltro", numeroProcesso);
        try {
            AnexosUseCases.ItemAnexo item = anexar.executar(new AnexosUseCases.AnexarArquivo.Comando(
                    numeroProcesso, arquivo.getOriginalFilename(), arquivo.getContentType(),
                    arquivo.getBytes(), descricao, oab));
            model.addAttribute("anexado", item.nome());
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            model.addAttribute("erro", falha.getMessage());
        }
        return montarTela(model, numeroProcesso);
    }

    private String montarTela(Model model, String processo) {
        model.addAttribute("anexos", listar.executar(processo));
        model.addAttribute("tipos", TipoArquivo.values());
        model.addAttribute("limiteMb", ArquivoAnexo.TAMANHO_MAXIMO_BYTES / (1024 * 1024));
        return "anexos";
    }
}
