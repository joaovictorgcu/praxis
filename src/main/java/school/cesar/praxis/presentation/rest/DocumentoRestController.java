package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.VersaoDocumento;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.presentation.web.seguranca.SessaoExigidaException;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de apresentacao (REST) da funcionalidade Geracao de documentos.
 *
 * <p>Revisar a peca e habilitar OAB nos autos sigilosos sao decisoes do chefe,
 * como nas telas: a anotacao vale aqui pela mesma guarda de sessao.
 *
 * <p>A OAB de quem age vem da sessao, nunca do corpo. A do
 * {@link OabDoCorpo} e outra coisa: e a OAB <em>de terceiro</em> que o chefe
 * habilita ou revoga nos autos, e continua sendo dado da requisicao.
 */
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
    private final DocumentosUseCases.EditarDocumento editarDocumento;
    private final DocumentosUseCases.RegenerarDocumento regenerarDocumento;
    private final DocumentosUseCases.RestaurarVersao restaurarVersao;
    private final DocumentosUseCases.ExcluirDocumento excluirDocumento;
    private final DocumentosUseCases.ConsultarVersoes consultarVersoes;

    public DocumentoRestController(DocumentosUseCases.GerarDocumento gerarDocumento,
                                   DocumentosUseCases.BaixarDocumento baixarDocumento,
                                   DocumentosUseCases.ListarDocumentos listarDocumentos,
                                   DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao,
                                   DocumentosUseCases.AprovarDocumento aprovarDocumento,
                                   DocumentosUseCases.RejeitarDocumento rejeitarDocumento,
                                   DocumentosUseCases.DesfazerDecisaoDocumento desfazerDecisao,
                                   DocumentosUseCases.ProtocolarDocumento protocolarDocumento,
                                   DocumentosUseCases.HabilitarOab habilitarOab,
                                   DocumentosUseCases.RevogarOab revogarOab,
                                   DocumentosUseCases.EditarDocumento editarDocumento,
                                   DocumentosUseCases.RegenerarDocumento regenerarDocumento,
                                   DocumentosUseCases.RestaurarVersao restaurarVersao,
                                   DocumentosUseCases.ExcluirDocumento excluirDocumento,
                                   DocumentosUseCases.ConsultarVersoes consultarVersoes) {
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
        this.editarDocumento = editarDocumento;
        this.regenerarDocumento = regenerarDocumento;
        this.restaurarVersao = restaurarVersao;
        this.excluirDocumento = excluirDocumento;
        this.consultarVersoes = consultarVersoes;
    }

    /** {@code codigoModelo} e opcional: sem ele, vale a peca compilada do tipo. */
    public record NovoDocumento(String numeroProcesso,
                                TipoDocumento tipo,
                                Map<String, String> campos,
                                String codigoModelo) {
    }

    public record DecisaoDocumento(String texto) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> gerar(@RequestBody NovoDocumento corpo,
                                                    UsuarioLogado usuario) {
        DocumentoGerado documento = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                corpo.numeroProcesso(), corpo.tipo(), corpo.campos(),
                usuario.oab(), corpo.codigoModelo(), usuario.nome()));

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

    @SomenteChefe
    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Map<String, Object>> aprovar(@PathVariable Long id,
                                                       @RequestBody DecisaoDocumento corpo,
                                                       UsuarioLogado usuario) {
        DocumentoGerado documento = aprovarDocumento.executar(
                new DocumentosUseCases.AprovarDocumento.Comando(id, usuario.oab(), corpo.texto()));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @SomenteChefe
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<Map<String, Object>> rejeitar(@PathVariable Long id,
                                                        @RequestBody DecisaoDocumento corpo,
                                                        UsuarioLogado usuario) {
        DocumentoGerado documento = rejeitarDocumento.executar(
                new DocumentosUseCases.RejeitarDocumento.Comando(id, usuario.oab(), corpo.texto()));
        return ResponseEntity.ok(Map.of("id", documento.getId(), "status", documento.getStatus().nome()));
    }

    @SomenteChefe
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

    @SomenteChefe
    @PostMapping("/{id}/oabs")
    public ResponseEntity<Map<String, Object>> habilitarOab(@PathVariable Long id, @RequestBody OabDoCorpo corpo) {
        DocumentoGerado documento = habilitarOab.executar(
                new DocumentosUseCases.HabilitarOab.Comando(id, corpo.oab()));
        return ResponseEntity.ok(Map.of(
                "id", documento.getId(),
                "segredoJustica", documento.isSegredoJustica(),
                "oabsHabilitadas", documento.getOabsHabilitadas()));
    }

    @SomenteChefe
    @DeleteMapping("/{id}/oabs/{oab}")
    public ResponseEntity<Map<String, Object>> revogarOab(@PathVariable Long id, @PathVariable String oab) {
        DocumentoGerado documento = revogarOab.executar(
                new DocumentosUseCases.RevogarOab.Comando(id, oab));
        return ResponseEntity.ok(Map.of(
                "id", documento.getId(),
                "segredoJustica", documento.isSegredoJustica(),
                "oabsHabilitadas", documento.getOabsHabilitadas()));
    }

    // --- Texto versionado ---

    public record NovoTexto(String conteudo, String resumo) {}

    public record CamposDaPeca(Map<String, String> campos) {}

    @PutMapping("/{id}/conteudo")
    public Map<String, Object> editar(@PathVariable Long id, @RequestBody NovoTexto corpo, UsuarioLogado usuario) {
        return versaoDe(editarDocumento.executar(new DocumentosUseCases.EditarDocumento.Comando(
                id, corpo.conteudo(), corpo.resumo(), usuario.oab(), usuario.nome())));
    }

    @PostMapping("/{id}/regenerar")
    public Map<String, Object> regenerar(@PathVariable Long id, @RequestBody CamposDaPeca corpo,
                                         UsuarioLogado usuario) {
        return versaoDe(regenerarDocumento.executar(new DocumentosUseCases.RegenerarDocumento.Comando(
                id, corpo.campos(), usuario.oab(), usuario.nome())));
    }

    @PostMapping("/{id}/versoes/{numero}/restaurar")
    public Map<String, Object> restaurar(@PathVariable Long id, @PathVariable int numero, UsuarioLogado usuario) {
        return versaoDe(restaurarVersao.executar(new DocumentosUseCases.RestaurarVersao.Comando(
                id, numero, usuario.oab(), usuario.nome())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, UsuarioLogado usuario) {
        excluirDocumento.executar(new DocumentosUseCases.ExcluirDocumento.Comando(
                id, usuario.oab(), usuario.chefe(), usuario.id(), usuario.nome()));
        return ResponseEntity.noContent().build();
    }

    /** Metadados das versoes; exige sessao, como a leitura da peca. */
    @GetMapping("/{id}/versoes")
    public List<DocumentosUseCases.ConsultarVersoes.ItemVersao> versoes(@PathVariable Long id,
                                                                        UsuarioLogado usuario) {
        exigirSessao(usuario);
        return consultarVersoes.listar(id);
    }

    /** Texto de uma versao: passa pelo Proxy de sigilo e fica na auditoria. */
    @GetMapping("/{id}/versoes/{numero}")
    public Map<String, Object> versao(@PathVariable Long id, @PathVariable int numero, UsuarioLogado usuario) {
        exigirSessao(usuario);
        VersaoDocumento versao = consultarVersoes.versao(id, numero, usuario.oab(), usuario.id(), usuario.nome());
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("documento", versao.documentoId());
        resposta.put("numero", versao.numero());
        resposta.put("origem", versao.origem().name());
        resposta.put("autorOab", versao.autorOab());
        resposta.put("criadaEm", versao.criadaEm().toString());
        resposta.put("resumo", versao.resumo());
        resposta.put("conteudo", versao.conteudo());
        return resposta;
    }

    @GetMapping("/{id}/comparar")
    public DocumentosUseCases.ConsultarVersoes.Comparacao comparar(@PathVariable Long id,
                                                                   @RequestParam int de,
                                                                   @RequestParam int para,
                                                                   UsuarioLogado usuario) {
        exigirSessao(usuario);
        return consultarVersoes.comparar(id, de, para, usuario.oab(), usuario.id(), usuario.nome());
    }

    private static void exigirSessao(UsuarioLogado usuario) {
        if (usuario == null) {
            throw new SessaoExigidaException("faça login para ler as versões desta peça");
        }
    }

    private static Map<String, Object> versaoDe(DocumentoGerado documento) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", documento.getId());
        resposta.put("status", documento.getStatus().nome());
        resposta.put("versaoAtual", documento.getVersaoAtual());
        resposta.put("caracteres", documento.getConteudo().length());
        return resposta;
    }

    @GetMapping
    public List<DocumentosUseCases.ListarDocumentos.ItemDocumento> listar(
            @RequestParam(required = false) String processo) {
        return listarDocumentos.executar(processo);
    }

    /**
     * Leitura protegida pelo Proxy: exige sessao e usa sempre a OAB de quem
     * esta logado, nunca uma informada na requisicao.
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> baixar(@PathVariable Long id, UsuarioLogado usuario) {
        if (usuario == null) {
            throw new SessaoExigidaException("faça login para baixar esta peça");
        }
        DocumentoGerado documento = baixarDocumento.executar(id, usuario.oab(), usuario.id(), usuario.nome());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documento.nomeArquivo() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(documento.getConteudo());
    }
}
