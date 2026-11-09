package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.prazo.ExecucaoVarredura;

import java.util.List;

/** Porta de saida: historico das execucoes da varredura ({@code varredura_execucao}). */
public interface ExecucaoVarreduraRepositorio {

    ExecucaoVarredura registrar(ExecucaoVarredura execucao);

    /** As {@code limite} execucoes mais recentes, da mais nova para a mais antiga. */
    List<ExecucaoVarredura> ultimas(int limite);
}
