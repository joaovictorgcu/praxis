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
    private final DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;
    private final DocumentosUseCases.AprovarDocumento aprovarDocumento;
    private final DocumentosUseCases.RejeitarDocumento rejeitarDocumento;
    private final DocumentosUseCases.DesfazerDecisaoDocumento desfazerDecisao;
    private final DocumentosUseCases.ProtocolarDocumento protocolarDocumento;
    private final DocumentosUseCases.HabilitarOab habilitarOab;
    private final DocumentosUseCases.RevogarOab revogarOab;

    public DocumentoRestController(DocumentosUseCases.GerarDocumento gerarDocumento,
                                   DocumentosUseCases.BaixarDocumento baixarDocumento,
                                   DocumentosUseCases.ListarDocumentos listarDocumentos,
                                   DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao,
                                   DocumentosUseCases.AprovarDocumento aprovarDocumento,
                                   DocumentosUseCases.RejeitarDocumento rejeitarDocumento,
                                   DocumentosUseCases.DesfazerDecisaoDocumento desfazerDecisao,
                                   DocumentosUseCases.ProtocolarDocumento protocolarDocumento,
                                   DocumentosUseCases.HabilitarOab habilitarOab,
                                   DocumentosUseCases.RevogarOab revogarOab) {
        this.gerarDocumento = gerarDocumento;
        this.baixarDocumento = baixarDocumento;
        this.listarDocumentos = listarDocumentos;
        this.enviarParaRevisao = enviarParaRevisao;
        this.aprovarDocumento = aprovarDocumento;
        this.rejeitarDocumento = rejeitarDocumento;
        this.desfazerDecisao = desfazerDecisao;
        this.protocolarDocumento = protocolarDocumento;
        this.habilitarOab = habilitarOab;
        this.revogarOab = revogarOab;
    }

    /** {@code codigoModelo} e opcional: sem ele, vale a peca compilada do tipo. */
    public record NovoDocumento(String numeroProcesso,
                                TipoDocumento tipo,
                                Map<String, String> campos,
                                String oabSolicitante,
                                String codigoModelo) {
    }

    public record DecisaoDocumento(String oab, String texto) {}

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


    @PostMapping("/{id}/enviar-revisao")
    public ResponseEntity<Map<String, Object>> enviarParaRevisao(@PathVariable Long id) {
        DocumentoGerado documento = enviarParaRevisao.executar(
                new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(id));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Map<String, Object>> aprovar(@PathVariable Long id, @RequestBody DecisaoDocumento corpo) {
        DocumentoGerado documento = aprovarDocumento.executar(
                new DocumentosUseCases.AprovarDocumento.Comando(id, corpo.oab(), corpo.texto()));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<Map<String, Object>> rejeitar(@PathVariable Long id, @RequestBody DecisaoDocumento corpo) {
        DocumentoGerado documento = rejeitarDocumento.executar(
                new DocumentosUseCases.RejeitarDocumento.Comando(id, corpo.oab(), corpo.texto()));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @PostMapping("/{id}/desfazer")
    public ResponseEntity<Map<String, Object>> desfazer(@PathVariable Long id) {
        DocumentoGerado documento = desfazerDecisao.executar(
                new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(id));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @PostMapping("/{id}/protocolar")
    public ResponseEntity<Map<String, Object>> protocolar(@PathVariable Long id) {
        DocumentoGerado documento = protocolarDocumento.executar(
                new DocumentosUseCases.ProtocolarDocumento.Comando(id));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    public record OabDoCorpo(String oab) {}

    @PostMapping("/{id}/oabs")
    public ResponseEntity<Map<String, Object>> habilitarOab(@PathVariable Long id, @RequestBody OabDoCorpo corpo) {
        DocumentoGerado documento = habilitarOab.executar(
                new DocumentosUseCases.HabilitarOab.Comando(id, corpo.oab()));
        return ResponseEntity.ok(Map.of(
                "id", documento.getId(),
                "segredoJustica", documento.isSegredoJustica(),
                "oabsHabilitadas", documento.getOabsHabilitadas()));
    }

    @DeleteMapping("/{id}/oabs/{oab}")
    public ResponseEntity<Map<String, Object>> revogarOab(@PathVariable Long id, @PathVariable String oab) {
        DocumentoGerado documento = revogarOab.executar(
                new DocumentosUseCases.RevogarOab.Comando(id, oab));
        return ResponseEntity.ok(Map.of(
                "id", documento.getId(),
                "segredoJustica", documento.isSegredoJustica(),
                "oabsHabilitadas", documento.getOabsHabilitadas()));
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
