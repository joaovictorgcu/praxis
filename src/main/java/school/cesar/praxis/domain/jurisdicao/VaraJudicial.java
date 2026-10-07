package school.cesar.praxis.domain.jurisdicao;

import java.time.LocalDate;

public record VaraJudicial(
    Long id,
    Long comarcaId,
    String nome,
    String competencia,
    String endereco,
    String telefone,
    String email,
    String horarioAtendimento,
    String observacoes,
    LocalDate dataCadastro
) {
    public VaraJudicial {
        if (comarcaId == null || comarcaId <= 0) {
            throw new IllegalArgumentException("Selecione a comarca da vara.");
        }
        nome = DadosJudiciarios.obrigatorio(nome, "Nome", 160);
        competencia = DadosJudiciarios.obrigatorio(competencia, "Competência", 120);
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
