package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.advogado.Advogado;

/** Advogado candidato a receber um processo novo, com os processos ativos calculados na hora. */
public record CandidatoDistribuicao(Advogado advogado, int processosAtivos) {
}
