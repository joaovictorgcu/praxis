package school.cesar.praxis.domain.compartilhado;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;

/**
 * Porta de saida: existe acesso ativo (concedido, nao revogado e nao
 * expirado em {@code hoje}) desta OAB a este processo? Implementada na
 * infraestrutura, consultando {@code AcessoAoProcesso}; o dominio so
 * enxerga o contrato. {@code hoje} vem de fora (o Relogio do projeto, na
 * borda da aplicacao) para o teste poder controlar o tempo.
 */
public interface VerificadorDeAcessoAoProcesso {

    boolean possuiAcessoAtivo(NumeroCnj processo, String oab, LocalDate hoje);
}
