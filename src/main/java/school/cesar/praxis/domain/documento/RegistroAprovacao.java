package school.cesar.praxis.domain.documento;

import java.time.LocalDateTime;

public class RegistroAprovacao {

    private final String deEstado;
    private final String paraEstado;
    private final String responsavelOab;
    private final String comentario;
    private final LocalDateTime quando;

    public RegistroAprovacao(String deEstado, String paraEstado, String responsavelOab,
                              String comentario, LocalDateTime quando) {
        this.deEstado = deEstado;
        this.paraEstado = paraEstado;
        this.responsavelOab = responsavelOab;
        this.comentario = comentario;
        this.quando = quando;
    }

    public String getDeEstado() { return deEstado; }
    public String getParaEstado() { return paraEstado; }
    public String getResponsavelOab() { return responsavelOab; }
    public String getComentario() { return comentario; }
    public LocalDateTime getQuando() { return quando; }
}