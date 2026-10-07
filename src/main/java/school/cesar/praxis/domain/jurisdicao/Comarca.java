package school.cesar.praxis.domain.jurisdicao;

import java.time.LocalDate;

public record Comarca(
    Long id,
    String nome,
    String municipio,
    String uf,
    String tribunal,
    String endereco,
    String telefone,
    String email,
    String horarioAtendimento,
    String observacoes,
    LocalDate dataCadastro
) {
    public Comarca {
        nome = DadosJudiciarios.obrigatorio(nome, "Nome", 160);
        municipio = DadosJudiciarios.obrigatorio(municipio, "Município", 120);
        uf = DadosJudiciarios.uf(uf);
        tribunal = DadosJudiciarios.obrigatorio(tribunal, "Tribunal", 120);
        endereco = DadosJudiciarios.opcional(endereco, "Endereço", 300);
        telefone = DadosJudiciarios.opcional(telefone, "Telefone", 30);
        email = DadosJudiciarios.email(email);
        horarioAtendimento = DadosJudiciarios.opcional(horarioAtendimento, "Horário de atendimento", 120);
        observacoes = DadosJudiciarios.opcional(observacoes, "Observações", 1000);
        if (dataCadastro == null) {
            throw new IllegalArgumentException("Data de cadastro é obrigatória.");
        }
    }
}
