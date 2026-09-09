package school.cesar.praxis.domain.anexo;

import java.util.Locale;

/**
 * Tipos aceitos na juntada. A lista e curta de proposito: os autos eletronicos
 * recebem peca em PDF e prova documental digitalizada, nao qualquer binario.
 * Recusar na porta evita anexo que o juizo nao consegue abrir.
 */
public enum TipoArquivo {

    PDF("application/pdf", "pdf", "PDF"),
    JPEG("image/jpeg", "jpg", "Imagem JPEG"),
    PNG("image/png", "png", "Imagem PNG"),
    TEXTO("text/plain", "txt", "Texto");

    private final String mime;
    private final String extensao;
    private final String rotulo;

    TipoArquivo(String mime, String extensao, String rotulo) {
        this.mime = mime;
        this.extensao = extensao;
        this.rotulo = rotulo;
    }

    /** Descobre o tipo pelo content-type recebido; recusa o que nao conhece. */
    public static TipoArquivo porMime(String mime) {
        String normalizado = mime == null ? "" : mime.trim().toLowerCase(Locale.ROOT);
        for (TipoArquivo tipo : values()) {
            if (tipo.mime.equals(normalizado)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("tipo de arquivo nao aceito na juntada: " + mime);
    }

    public String mime() {
        return mime;
    }

    public String extensao() {
        return extensao;
    }

    public String rotulo() {
        return rotulo;
    }
}
