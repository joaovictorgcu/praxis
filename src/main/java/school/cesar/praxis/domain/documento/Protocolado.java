package school.cesar.praxis.domain.documento;

public class Protocolado implements StatusDocumento {

    @Override
    public StatusDocumento enviarParaRevisao(DocumentoGerado documento) {
        return transicaoInvalida("enviar para revisao");
    }

    @Override
    public StatusDocumento aprovar(DocumentoGerado documento, String oabAprovador, String comentario) {
        return transicaoInvalida("aprovar");
    }

    @Override
    public StatusDocumento rejeitar(DocumentoGerado documento, String oabAprovador, String motivo) {
        return transicaoInvalida("rejeitar");
    }

    @Override
    public StatusDocumento protocolar(DocumentoGerado documento) {
        return transicaoInvalida("protocolar");
    }

    @Override
    public String nome() {
        return "PROTOCOLADO";
    }
}