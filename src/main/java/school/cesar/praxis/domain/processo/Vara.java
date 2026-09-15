package school.cesar.praxis.domain.processo;

/** Informacao da vara do processo, usada para exibicao e organizacao de audiencias e prazos. */
public record Vara(String tribunal, String comarca, String numero) {
}
