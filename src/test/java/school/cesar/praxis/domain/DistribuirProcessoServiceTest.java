package school.cesar.praxis.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import school.cesar.praxis.domain.distribuicao.*;
import school.cesar.praxis.domain.processo.Advogado;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
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

        advogadoEspecialista = mock(Advogado.class);
        advogadoDisponivel = mock(Advogado.class);
        advogadoPadrao = mock(Advogado.class);
    }

    // --- COBERTURA: RegraPadrao ---

    @Test
    @DisplayName("RegraPadrao: Deve escolher o candidato com menor número de processos ativos")
    void deveDistribuirPorRegraPadrao() {
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoEspecialista, "Civil", 10, true);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoPadrao, "Civil", 2, true);

        List<CandidatoDistribuicao> candidatos = List.of(c1, c2);

        Advogado escolhido = regraPadrao.distribuir("Civil", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoPadrao, escolhido);
    }

    // --- COBERTURA: RegraPorEspecialidade ---

    @Test
    @DisplayName("RegraPorEspecialidade: Deve distribuir para o advogado com a especialidade correspondente")
    void deveDistribuirPorEspecialidade() {
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoEspecialista, "Trabalhista", 5, true);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoPadrao, "Civil", 1, true);

        List<CandidatoDistribuicao> candidatos = List.of(c1, c2);

        Advogado escolhido = regraPorEspecialidade.distribuir("Trabalhista", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoEspecialista, escolhido);
    }

    // --- COBERTURA: RegraPorDisponibilidade ---

    @Test
    @DisplayName("RegraPorDisponibilidade: Deve distribuir apenas para advogados disponíveis")
    void deveDistribuirPorDisponibilidade() {
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoPadrao, "Civil", 1, false);
        CandidatoDistribuicao c2 = new CandidatoDistribuicao(advogadoDisponivel, "Civil", 4, true);

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

        // Advogado sem especialidade solicitada e indisponível, caindo na RegraPadrao como fallback
        CandidatoDistribuicao c1 = new CandidatoDistribuicao(advogadoPadrao, "Civil", 3, false);

        List<CandidatoDistribuicao> candidatos = List.of(c1);

        Advogado escolhido = regraPorEspecialidade.distribuir("Tributario", candidatos);

        assertNotNull(escolhido);
        assertEquals(advogadoPadrao, escolhido);
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