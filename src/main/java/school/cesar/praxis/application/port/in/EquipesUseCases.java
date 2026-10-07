package school.cesar.praxis.application.port.in;

import java.util.List;
import java.util.Set;

public interface EquipesUseCases {
    record ItemEquipe(Long id, String nome, Set<Long> membrosIds) {}

    interface CadastrarEquipe {
        record Comando(String nome, Set<Long> membrosIds) {
            public Comando(String nome) {
                this(nome, Set.of());
            }
        }

        ItemEquipe executar(Comando comando);
    }

    interface AtualizarEquipe {
        record Comando(Long id, String nome, Set<Long> membrosIds) {}

        ItemEquipe executar(Comando comando);
    }

    interface ExcluirEquipe {
        void executar(Long id);
    }

    interface ListarEquipes {
        List<ItemEquipe> executar();
    }

    interface AdicionarMembro {
        ItemEquipe executar(Long equipeId, Long advogadoId);
    }

    interface RemoverMembro {
        ItemEquipe executar(Long equipeId, Long advogadoId);
    }
}
