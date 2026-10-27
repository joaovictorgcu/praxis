package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.presentation.web.seguranca.SessaoExigidaException;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.io.IOException;
import java.util.List;

/**
 * Camada de apresentacao (REST) da funcionalidade Anexacao de arquivos.
 *
 * <p>Juntada e download carregam a OAB da sessao: quem age e quem esta
 * logado, nunca quem a requisicao disser que e. O download deixou de aceitar
 * {@code ?oab=} livre - sem sessao e 401, porque um chamador anonimo que
 * soubesse o id e uma OAB habilitada conseguia ler autos sigilosos.
 */
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
                                           UsuarioLogado usuario)
            throws IOException {
        return anexar.executar(new AnexosUseCases.AnexarArquivo.Comando(
                numeroProcesso,
                arquivo.getOriginalFilename(),
                arquivo.getContentType(),
                arquivo.getBytes(),
                descricao,
                usuario.oab()));
    }

    @GetMapping
    public List<AnexosUseCases.ItemAnexo> listar(@RequestParam(required = false) String processo) {
        return listar.executar(processo);
    }

    /**
     * Leitura protegida pelo Proxy: exige sessao e usa sempre a OAB de quem
     * esta logado, nunca uma informada na requisicao.
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id, UsuarioLogado usuario) {
        if (usuario == null) {
            throw new SessaoExigidaException("faça login para baixar este arquivo");
        }
        ArquivoAnexo anexo = baixar.executar(id, usuario.oab(), usuario.id(), usuario.nome());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + anexo.getNome() + "\"")
                .contentType(MediaType.parseMediaType(anexo.getTipo().mime()))
                .body(anexo.getConteudo());
    }
}
