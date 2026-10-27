package school.cesar.praxis.domain.compartilhado;

import java.time.LocalDate;

/**
 * Unica regra de "pode ler os autos?", usada pelo {@link ProxyDeAcesso} para
 * peca e anexo - nao duplicada em nenhum outro lugar. Processo publico: o
 * proprio {@link ConteudoRestrito#podeSerLidoPor} ja responde sim. Em segredo
 * de justica: a OAB precisa estar habilitada nesta peca/anexo (idem) OU ter
 * acesso ativo e nao expirado ao processo inteiro.
 */
public final class PoliticaDeLeituraDosAutos {

    private PoliticaDeLeituraDosAutos() {
    }

    public static boolean podeLer(ConteudoRestrito conteudo, String oabSolicitante,
                                  VerificadorDeAcessoAoProcesso verificadorAcesso, LocalDate hoje) {
        if (conteudo.podeSerLidoPor(oabSolicitante)) {
            return true;
        }
        if (oabSolicitante == null || oabSolicitante.isBlank() || verificadorAcesso == null) {
            return false;
        }
        return verificadorAcesso.possuiAcessoAtivo(conteudo.getNumeroProcesso(), oabSolicitante, hoje);
    }
}
