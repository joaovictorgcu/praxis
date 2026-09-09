package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

import java.io.IOException;
import java.util.List;

/** Camada de apresentacao (REST) da funcionalidade Anexacao de arquivos. */
@RestController
@RequestMapping("/api/anexos")
public class AnexoRestController {

    private final AnexosUseCases.AnexarArquivo anexar;
    private final AnexosUseCases.ListarAnexos listar;
    private final AnexosUseCases.BaixarAnexo baixar;

    public AnexoRestController(AnexosUseCases.AnexarArquivo anexar,
                               AnexosUseCases.ListarAnexos listar,
                               AnexosUseCases.BaixarAnexo baixar) {
        this.anexar = anexar;
        this.listar = listar;
        this.baixar = baixar;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnexosUseCases.ItemAnexo anexar(@RequestParam String numeroProcesso,
                                           @RequestParam MultipartFile arquivo,
                                           @RequestParam(required = false) String descricao,
                                           @RequestParam(required = false) String oab)
            throws IOException {
        return anexar.executar(new AnexosUseCases.AnexarArquivo.Comando(
                numeroProcesso,
                arquivo.getOriginalFilename(),
                arquivo.getContentType(),
                arquivo.getBytes(),
                descricao,
                oab));
    }

    @GetMapping
    public List<AnexosUseCases.ItemAnexo> listar(@RequestParam(required = false) String processo) {
        return listar.executar(processo);
    }

    /** Leitura protegida pelo Proxy: exige a OAB do solicitante. */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id,
                                         @RequestParam(name = "oab") String oab) {
        try {
            ArquivoAnexo anexo = baixar.executar(id, oab);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + anexo.getNome() + "\"")
                    .contentType(MediaType.parseMediaType(anexo.getTipo().mime()))
                    .body(anexo.getConteudo());
        } catch (ProxyDeAcesso.AcessoNegadoException negado) {
            return ResponseEntity.status(403)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(negado.getMessage().getBytes());
        }
    }
}
