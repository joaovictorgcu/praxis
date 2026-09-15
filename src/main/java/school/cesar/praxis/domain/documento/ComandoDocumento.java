package school.cesar.praxis.domain.documento;

public interface ComandoDocumento {
    void executar();
    void desfazer();
    String descricao();
    DocumentoGerado documento();
}