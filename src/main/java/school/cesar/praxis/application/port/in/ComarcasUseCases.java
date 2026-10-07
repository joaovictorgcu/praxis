package school.cesar.praxis.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface ComarcasUseCases {
    record ItemComarca(
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
    ) {}

    interface Cadastrar {
        record Comando(
            String nome,
            String municipio,
            String uf,
            String tribunal,
            String endereco,
            String telefone,
            String email,
            String horarioAtendimento,
            String observacoes
        ) {}

        ItemComarca executar(Comando comando);
    }

    interface Atualizar {
        record Comando(
            Long id,
            String nome,
            String municipio,
            String uf,
            String tribunal,
            String endereco,
            String telefone,
            String email,
            String horarioAtendimento,
            String observacoes
        ) {}

        ItemComarca executar(Comando comando);
    }

    interface Listar {
        List<ItemComarca> executar();
    }

    interface BuscarPorId {
        ItemComarca executar(Long id);
    }
}
