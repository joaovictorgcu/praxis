package school.cesar.praxis.domain.advogado;

import java.time.LocalDate;

public class Advogado {

    private final Long id;
    private String nome;
    private String email;
    private final String oab;
    private String telefone;
    private String especialidade;
    private StatusAdvogado status;
    private boolean disponivel;
    private final LocalDate dataAdmissao;

    public Advogado(Long id,
                    String nome,
                    String email,
                    String oab,
                    String telefone,
                    String especialidade,
                    StatusAdvogado status,
                    boolean disponivel,
                    LocalDate dataAdmissao) {
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("OAB e obrigatoria");
        }
        if (status == null) {
            throw new IllegalArgumentException("status do advogado e obrigatorio");
        }
        if (dataAdmissao == null) {
            throw new IllegalArgumentException("data de admissao e obrigatoria");
        }
        this.id = id;
        this.oab = oab.trim();
        this.status = status;
        this.dataAdmissao = dataAdmissao;
        atualizarDados(nome, email, telefone, especialidade, disponivel);
    }

    public Advogado(String nome,
                    String email,
                    String oab,
                    String telefone,
                    String especialidade,
                    boolean disponivel,
                    LocalDate dataAdmissao) {
        this(null, nome, email, oab, telefone, especialidade, StatusAdvogado.ATIVO, disponivel, dataAdmissao);
    }

    public void atualizarDados(String nome, String email, String telefone, String especialidade, boolean disponivel) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do advogado e obrigatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("e-mail do advogado invalido: " + email);
        }
        this.nome = nome.trim();
        this.email = email.trim();
        this.telefone = telefone == null || telefone.isBlank() ? null : telefone.trim();
        this.especialidade = especialidade == null || especialidade.isBlank() ? null : especialidade.trim();
        this.disponivel = disponivel;
    }

    public void ativar() {
        this.status = StatusAdvogado.ATIVO;
    }

    public void desativar() {
        this.status = StatusAdvogado.DESATIVADO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getOab() {
        return oab;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public StatusAdvogado getStatus() {
        return status;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }
}
