package school.cesar.praxis.domain.honorario;

import java.time.LocalDateTime;

public class RegistroHonorario {

    private final String tipo;
    private final String descricao;
    private final LocalDateTime quando;

    public RegistroHonorario(String tipo, String descricao, LocalDateTime quando) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.quando = quando;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getQuando() {
        return quando;
    }
}
