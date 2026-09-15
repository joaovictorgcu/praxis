package school.cesar.praxis.domain.documento;

public class ComandoAprovarDocumento implements ComandoDocumento {

    private final DocumentoGerado documento;
    private final String oabAprovador;
    private final String comentario;
    private StatusDocumento statusAnterior;

    public ComandoAprovarDocumento(DocumentoGerado documento, String oabAprovador, String comentario) {
        this.documento = documento;
        this.oabAprovador = oabAprovador;
        this.comentario = comentario;
    }

    @Override
    public void executar() {
        this.statusAnterior = documento.getStatus();
        documento.aprovar(oabAprovador, comentario);
    }

    @Override
    public void desfazer() {
        if (statusAnterior == null) {
            throw new IllegalStateException("comando ainda nao foi executado");
        }
        documento.restaurarStatus(statusAnterior, oabAprovador, "desfeito: aprovacao revertida");
    }

    @Override
    public String descricao() {
        return "Aprovar documento " + documento.nomeArquivo() + " por " + oabAprovador;
    }

    @Override
    public DocumentoGerado documento() {
        return documento;
    }
}