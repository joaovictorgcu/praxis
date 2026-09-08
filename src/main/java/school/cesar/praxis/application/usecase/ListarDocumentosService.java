package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;

/** Caso de uso de leitura: pecas ja geradas (metadados, sem conteudo). */
@Service
public class ListarDocumentosService implements DocumentosUseCases.ListarDocumentos {

    private final DocumentoRepositorio documentos;

    public ListarDocumentosService(DocumentoRepositorio documentos) {
        this.documentos = documentos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDocumento> executar(String numeroProcesso) {
        List<DocumentoGerado> encontrados = numeroProcesso == null || numeroProcesso.isBlank()
                ? documentos.listar()
                : documentos.porProcesso(NumeroCnj.de(numeroProcesso));

        return encontrados.stream()
                .map(documento -> new ItemDocumento(
                        documento.getId(),
                        documento.getNumeroProcesso().valor(),
                        documento.getTipo(),
                        String.valueOf(documento.getGeradoEm()),
                        documento.isSegredoJustica()))
                .toList();
    }
}
