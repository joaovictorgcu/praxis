package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;

public interface DistribuicaoUseCases {

    interface DistribuirProcesso {

        record Candidato(String nome,
                         String email,
                         String oab,
                         String especialidade,
                         int processosAtivos,
                         boolean disponivel) {
        }

        record Comando(String numeroProcesso,
                       String areaDireito,
                       List<Candidato> candidatos) {
        }

        Advogado executar(Comando comando);
    }
}