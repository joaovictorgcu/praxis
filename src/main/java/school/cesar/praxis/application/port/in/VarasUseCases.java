package school.cesar.praxis.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface VarasUseCases {
    record ItemVara(
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
    ) {}

    interface Cadastrar {
        record Comando(
            Long comarcaId,
            String nome,
            String competencia,
            String endereco,
            String telefone,
            String email,
            String horarioAtendimento,
            String observacoes
        ) {}

        ItemVara executar(Comando comando);
    }

    interface Atualizar {
        record Comando(
            Long id,
            Long comarcaId,
            String nome,
            String competencia,
            String endereco,
            String telefone,
            String email,
            String horarioAtendimento,
            String observacoes
        ) {}

        ItemVara executar(Comando comando);
    }

    interface Listar {
        List<ItemVara> executar();
    }

    interface BuscarPorId {
        ItemVara executar(Long id);
    }
}
