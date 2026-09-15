package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.advogado.Advogado;

public interface DistribuicaoUseCases {

    interface DistribuirProcesso {

        record Comando(String numeroProcesso, String areaDireito, Long equipeId) {
        }

        Advogado executar(Comando comando);
    }
}
