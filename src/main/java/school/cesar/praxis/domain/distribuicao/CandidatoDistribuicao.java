package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.processo.Advogado;

/** Advogado candidato a receber um processo novo, com dados usados pelas regras. */
public record CandidatoDistribuicao(Advogado advogado,
                                    String especialidade,
                                    int processosAtivos,
                                    boolean disponivel) {
}