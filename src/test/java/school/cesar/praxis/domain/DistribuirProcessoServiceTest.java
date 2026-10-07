package school.cesar.praxis.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import school.cesar.praxis.domain.advogado.Advogado;
import school.cesar.praxis.domain.distribuicao.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistribuirProcessoServiceTest {

    private RegraPadrao regraPadrao;
    private RegraPorEspecialidade regraPorEspecialidade;
    private RegraPorDisponibilidade regraPorDisponibilidade;

    private Advogado advogadoEspecialista;
    private Advogado advogadoDisponivel;
    private Advogado advogadoPadrao;

    @BeforeEach
    void setUp() {
        regraPadrao = new RegraPadrao();
        regraPorEspecialidade = new RegraPorEspecialidade();
        regraPorDisponibilidade = new RegraPorDisponibilidade();

        advogadoEspecialista = advogado("Trabalhista", true);
        advogadoDisponivel = advogado("Civil", true);
        advogadoPadrao = advogado("Civil", true);
    }

    private static Advogado advogado(String especialidade, boolean disponivel) {
        return new Advogado("Advogado Teste", "advogado@praxis.adv.br", "PE00000",
                null, especialidade, disponivel, LocalDate.now());
    }

    // --- COBERTURA: RegraPadrao ---

    @Test
    @DisplayName("RegraPadrao: Deve escolher o candidato com menor número de processos ativos")
    void deveDistribuirPorRegraPadrao() {
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoEspecialista, 10);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoPadrao, 2);

        List<CandidatoDistribuicao> candidatos = List.of(c1, c2);

        Advogado escolhido = regraPadrao.distribuir("Civil", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoPadrao, escolhido);
    }

    // --- COBERTURA: RegraPorEspecialidade ---

    @Test
    @DisplayName("RegraPorEspecialidade: Deve distribuir para o advogado com a especialidade correspondente")
    void deveDistribuirPorEspecialidade() {
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoEspecialista, 5);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoPadrao, 1);

        List<CandidatoDistribuicao> candidatos = List.of(c1, c2);

        Advogado escolhido = regraPorEspecialidade.distribuir("Trabalhista", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoEspecialista, escolhido);
    }

    // --- COBERTURA: RegraPorDisponibilidade ---

    @Test
    @DisplayName("RegraPorDisponibilidade: Deve distribuir apenas para advogados disponíveis")
    void deveDistribuirPorDisponibilidade() {
        Advogado indisponivel = advogado("Civil", false);
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(indisponivel, 1);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoDisponivel, 4);

        List<CandidatoDistribuicao> candidatos = List.of(c1, c2);

        Advogado escolhido = regraPorDisponibilidade.distribuir("Penal", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoDisponivel, escolhido);
    }

    // --- COBERTURA: Cadeia de Responsabilidade Completa ---

    @Test
    @DisplayName("Cadeia: Deve encadear Especialidade -> Disponibilidade -> Padrao")
    void deveExecutarCadeiaCompletaAteFallback() {
        regraPorEspecialidade.proximaRegra(regraPorDisponibilidade);
        regraPorDisponibilidade.proximaRegra(regraPadrao);

        Advogado indisponivel = advogado("Civil", false);
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(indisponivel, 3);

        List<CandidatoDistribuicao> candidatos = List.of(c1);

        Advogado escolhido = regraPorEspecialidade.distribuir("Tributario", candidatos);

        assertNotNull(escolhido);
        assertEquals(indisponivel, escolhido);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a lista de candidatos for vazia")
    void deveLancarExcecaoQuandoNaoHouverAdvogados() {
        List<CandidatoDistribuicao> candidatos = Collections.emptyList();

        assertThrows(IllegalStateException.class, () -> {
            regraPadrao.distribuir("Civil", candidatos);
        });
    }
}
