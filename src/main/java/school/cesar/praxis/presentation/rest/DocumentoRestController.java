package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.documento.TipoDocumento;

import java.util.List;
import java.util.Map;

/** Camada de apresentacao (REST) da funcionalidade Geracao de documentos. */
@RestController
@RequestMapping("/api/documentos")
public class DocumentoRestController {

    private final DocumentosUseCases.GerarDocumento gerarDocumento;
    private final DocumentosUseCases.BaixarDocumento baixarDocumento;
    private final DocumentosUseCases.ListarDocumentos listarDocumentos;

    public DocumentoRestController(DocumentosUseCases.GerarDocumento gerarDocumento,
                                   DocumentosUseCases.BaixarDocumento baixarDocumento,
                                   DocumentosUseCases.ListarDocumentos listarDocumentos) {
        this.gerarDocumento = gerarDocumento;
        this.baixarDocumento = baixarDocumento;
        this.listarDocumentos = listarDocumentos;
    }

    /** {@code codigoModelo} e opcional: sem ele, vale a peca compilada do tipo. */
    public record NovoDocumento(String numeroProcesso,
                                TipoDocumento tipo,
                                Map<String, String> campos,
                                String oabSolicitante,
                                String codigoModelo) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> gerar(@RequestBody NovoDocumento corpo) {
        DocumentoGerado documento = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                corpo.numeroProcesso(), corpo.tipo(), corpo.campos(),
                corpo.oabSolicitante(), corpo.codigoModelo()));

        return ResponseEntity.ok(Map.of(
                "id", documento.getId(),
                "processo", documento.getNumeroProcesso().valor(),
                "tipo", documento.getTipo().name(),
                "arquivo", documento.nomeArquivo(),
                "segredoJustica", documento.isSegredoJustica(),
                "caracteres", documento.getConteudo().length()));
    }

    @GetMapping
    public List<DocumentosUseCases.ListarDocumentos.ItemDocumento> listar(
            @RequestParam(required = false) String processo) {
        return listarDocumentos.executar(processo);
    }

    /** Leitura protegida pelo Proxy: exige a OAB do solicitante. */
    @GetMapping("/{id}")
    public ResponseEntity<String> baixar(@PathVariable Long id,
                                         @RequestParam(name = "oab") String oab) {
        try {
            DocumentoGerado documento = baixarDocumento.executar(id, oab);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + documento.nomeArquivo() + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(documento.getConteudo());
        } catch (ProxyDeAcesso.AcessoNegadoException negado) {
            return ResponseEntity.status(403).body(negado.getMessage());
        }
    }
}
