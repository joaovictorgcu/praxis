package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.advogado.StatusAdvogado;

import java.time.LocalDate;
import java.util.List;

public interface AdvogadosUseCases {

    record ItemAdvogado(Long id,
                        String nome,
                        String email,
                        String oab,
                        String telefone,
                        String especialidade,
                        StatusAdvogado status,
                        boolean disponivel,
                        LocalDate dataAdmissao) {
    }

    interface CadastrarAdvogado {

        record Comando(String nome,
                       String email,
                       String oab,
                       String telefone,
                       String especialidade,
                       boolean disponivel) {
        }

        ItemAdvogado executar(Comando comando);
    }

    interface ListarAdvogados {

        List<ItemAdvogado> executar();
    }

    interface PesquisarAdvogados {

        List<ItemAdvogado> executar(String termo);
    }

    interface BuscarAdvogadoPorId {

        ItemAdvogado executar(Long id);
    }

    interface AtualizarAdvogado {

        record Comando(Long id,
                       String nome,
                       String email,
                       String telefone,
                       String especialidade,
                       boolean disponivel) {
        }

        ItemAdvogado executar(Comando comando);
    }

    interface AtivarAdvogado {

        ItemAdvogado executar(Long id);
    }

    interface DesativarAdvogado {

        ItemAdvogado executar(Long id);
    }
}
