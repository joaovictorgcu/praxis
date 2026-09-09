package school.cesar.praxis.domain.documento;

public class ComandoRejeitarDocumento implements ComandoDocumento {

    private final DocumentoGerado documento;
    private final String oabAprovador;
    private final String motivo;
    private StatusDocumento statusAnterior;

    public ComandoRejeitarDocumento(DocumentoGerado documento, String oabAprovador, String motivo) {
        this.documento = documento;
        this.oabAprovador = oabAprovador;
        this.motivo = motivo;
    }

    @Override
    public void executar() {
        this.statusAnterior = documento.getStatus();
        documento.rejeitar(oabAprovador, motivo);
    }

    @Override
    public void desfazer() {
        if (statusAnterior == null) {
            throw new IllegalStateException("comando ainda nao foi executado");
        }
        documento.restaurarStatus(statusAnterior, oabAprovador, "desfeito: rejeicao revertida");
    }

    @Override
    public String descricao() {
        return "Rejeitar documento " + documento.nomeArquivo() + " por " + oabAprovador;
    }

    @Override
    public DocumentoGerado documento() {
        return documento;
    }
}