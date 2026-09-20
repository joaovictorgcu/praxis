package school.cesar.praxis.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.junit.jupiter.api.Assertions;
import school.cesar.praxis.domain.distribuicao.CandidatoDistribuicao;
import school.cesar.praxis.domain.distribuicao.RegraPadrao;
import school.cesar.praxis.domain.distribuicao.RegraPorDisponibilidade;
import school.cesar.praxis.domain.distribuicao.RegraPorEspecialidade;
import school.cesar.praxis.domain.processo.Advogado;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DistribuicaoSteps {

    private final List<CandidatoDistribuicao> candidatos = new ArrayList<>();
    private Advogado advogadoAtribuido;

    @Dado("que existem os seguintes advogados cadastrados:")
    public void que_existem_os_seguintes_advogados_cadastrados(DataTable dataTable) {
        candidatos.clear();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String nome = getValor(row, "nome", "Nome");
            String especialidade = getValor(row, "especialidade", "Especialidade");

            int processosAtivos = 0;
            String valProcessos = getValor(row, "processosAtivos", "processos_ativos", "cargaTrabalho", "carga_trabalho");
            if (valProcessos != null) {
                processosAtivos = Integer.parseInt(valProcessos);
            }

            boolean disponivel = true;
            String valDisponivel = getValor(row, "disponivel", "disponível");
            if (valDisponivel != null) {
                disponivel = Boolean.parseBoolean(valDisponivel);
            }

            Advogado advMock = mock(Advogado.class);
            when(advMock.toString()).thenReturn(nome);

            CandidatoDistribuicao candidato = new CandidatoDistribuicao(advMock, especialidade, processosAtivos, disponivel);
            candidatos.add(candidato);
        }
    }

    @Quando("o sistema solicita a distribuição de um processo da área {string}")
    public void o_sistema_solicita_a_distribuição_de_um_processo_da_área(String area) {
        RegraPorEspecialidade regraEspecialidade = new RegraPorEspecialidade();
        RegraPorDisponibilidade regraDisponibilidade = new RegraPorDisponibilidade();
        RegraPadrao regraPadrao = new RegraPadrao();

        regraEspecialidade.proximaRegra(regraDisponibilidade);
        regraDisponibilidade.proximaRegra(regraPadrao);

        this.advogadoAtribuido = regraEspecialidade.distribuir(area, candidatos);
    }

    @Então("o processo deve ser atribuído ao advogado {string}")
    public void o_processo_deve_ser_atribuído_ao_advogado(String nomeEsperado) {
        Assertions.assertNotNull(this.advogadoAtribuido, "Nenhum advogado foi atribuído ao processo");
        Assertions.assertEquals(nomeEsperado, this.advogadoAtribuido.toString());
    }

    private String getValor(Map<String, String> row, String... chaves) {
        for (String chave : chaves) {
            if (row.containsKey(chave)) {
                return row.get(chave);
            }
        }
        return null;
    }
}