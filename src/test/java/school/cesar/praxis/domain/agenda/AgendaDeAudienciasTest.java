package school.cesar.praxis.domain.agenda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.compartilhado.Relogio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Agenda de Audiências - Testes de Domínio")
class AgendaDeAudienciasTest {

    private AgendaDeAudiencias agenda;
    private Audiencia audiencia1;
    private Audiencia audiencia2;

    @BeforeEach
    void configurar() {
        agenda = new AgendaDeAudiencias();
        
        // Audiência 1: 10:00 às 11:00
        audiencia1 = new Audiencia(
            "Proc. 001/2024",
            "João Silva",
            LocalDateTime.of(2024, 9, 15, 10, 0),
            LocalDateTime.of(2024, 9, 15, 11, 0),
            "Sala 1"
        );

        // Audiência 2: 14:00 às 15:00 (sem conflito)
        audiencia2 = new Audiencia(
            "Proc. 002/2024",
            "Maria Santos",
            LocalDateTime.of(2024, 9, 15, 14, 0),
            LocalDateTime.of(2024, 9, 15, 15, 0),
            "Sala 1"
        );
    }

    @Test
    @DisplayName("Deve criar uma audiência válida sem conflitos")
    void criarAudienciaValida() {
        agenda.adicionarAudiencia(audiencia1);
        assertTrue(agenda.contemAudiencia(audiencia1));
    }

    @Test
    @DisplayName("Deve detectar conflito quando audiência sobrepõe horário")
    void detectarConflitoPorSobreposicaoDeHorario() {
        agenda.adicionarAudiencia(audiencia1);
        
        // Audiência que sobrepõe (10:30 às 11:30)
        Audiencia audienciaComConflito = new Audiencia(
            "Proc. 003/2024",
            "Pedro Costa",
            LocalDateTime.of(2024, 9, 15, 10, 30),
            LocalDateTime.of(2024, 9, 15, 11, 30),
            "Sala 1"
        );

        assertTrue(agenda.temConflito(audienciaComConflito),
            "Deve detectar conflito de horário na mesma sala");
    }

    @Test
    @DisplayName("Deve permitir audiências em horários diferentes sem conflito")
    void permitirAudienciaSemConflito() {
        agenda.adicionarAudiencia(audiencia1);
        agenda.adicionarAudiencia(audiencia2);
        
        assertFalse(agenda.temConflito(audiencia2));
        assertEquals(2, agenda.quantidadeDeAudiencias());
    }

    @Test
    @DisplayName("Deve impedir adicionar audiência quando há conflito")
    void impedirAdicaoComConflito() {
        agenda.adicionarAudiencia(audiencia1);
        
        Audiencia audienciaComConflito = new Audiencia(
            "Proc. 004/2024",
            "Ana Lima",
            LocalDateTime.of(2024, 9, 15, 10, 15),
            LocalDateTime.of(2024, 9, 15, 10, 45),
            "Sala 1"
        );

        assertThrows(ConflitoDEAudienciaException.class,
            () -> agenda.adicionarAudiencia(audienciaComConflito),
            "Deve lançar exceção ao detectar conflito");
    }

    @Test
    @DisplayName("Deve permitir audiências na mesma hora em salas diferentes")
    void permitirAudienciasEmSalasDiferentes() {
        agenda.adicionarAudiencia(audiencia1);
        
        Audiencia audienciaOutraSala = new Audiencia(
            "Proc. 005/2024",
            "Carlos Mendes",
            LocalDateTime.of(2024, 9, 15, 10, 0),
            LocalDateTime.of(2024, 9, 15, 11, 0),
            "Sala 2"
        );

        assertFalse(agenda.temConflito(audienciaOutraSala),
            "Não deve haver conflito em salas diferentes");
        agenda.adicionarAudiencia(audienciaOutraSala);
        assertEquals(2, agenda.quantidadeDeAudiencias());
    }

    @Test
    @DisplayName("Deve encontrar audiência pelo ID")
    void encontrarAudienciaPorId() {
        audiencia1.atribuirId(1L);
        agenda.adicionarAudiencia(audiencia1);
        
        Audiencia encontrada = agenda.encontrarPorId(1L);
        assertNotNull(encontrada);
        assertEquals(audiencia1.getNumeroProcesso(), encontrada.getNumeroProcesso());
    }

    @Test
    @DisplayName("Deve listar todas as audiências")
    void listarTodasAsAudiencias() {
        agenda.adicionarAudiencia(audiencia1);
        agenda.adicionarAudiencia(audiencia2);
        
        List<Audiencia> audiencias = agenda.listarTodas();
        assertEquals(2, audiencias.size());
    }

    @Test
    @DisplayName("Deve listar audiências por data")
    void listarAudienciasPorData() {
        agenda.adicionarAudiencia(audiencia1);
        agenda.adicionarAudiencia(audiencia2);
        
        List<Audiencia> audienciasDodia = agenda.listarPorData(
            LocalDateTime.of(2024, 9, 15, 0, 0),
            LocalDateTime.of(2024, 9, 15, 23, 59)
        );
        
        assertEquals(2, audienciasDodia.size());
    }

    @Test
    @DisplayName("Deve editar uma audiência existente sem criar conflito")
    void editarAudienciaValida() {
        audiencia1.atribuirId(1L);
        agenda.adicionarAudiencia(audiencia1);
        
        Audiencia audienciaEditada = new Audiencia(
            "Proc. 001/2024", // Mesmo número de processo
            "João Silva Editado",
            LocalDateTime.of(2024, 9, 15, 11, 30),
            LocalDateTime.of(2024, 9, 15, 12, 30),
            "Sala 2"
        );
        audienciaEditada.atribuirId(1L);
        
        agenda.atualizar(audienciaEditada);
        Audiencia encontrada = agenda.encontrarPorId(1L);
        assertEquals("João Silva Editado", encontrada.getNomeParteAutora());
        assertEquals("Sala 2", encontrada.getSala());
    }

    @Test
    @DisplayName("Deve remover uma audiência")
    void removerAudiencia() {
        audiencia1.atribuirId(1L);
        agenda.adicionarAudiencia(audiencia1);
        assertEquals(1, agenda.quantidadeDeAudiencias());
        
        agenda.remover(1L);
        assertEquals(0, agenda.quantidadeDeAudiencias());
    }

    @Test
    @DisplayName("Deve validar que horário inicial é antes do horário final")
    void validarHorarios() {
        assertThrows(HorarioInvalidoException.class,
            () -> new Audiencia(
                "Proc. 006/2024",
                "Paula Gomes",
                LocalDateTime.of(2024, 9, 15, 15, 0),
                LocalDateTime.of(2024, 9, 15, 14, 0),
                "Sala 1"
            ),
            "Deve lançar exceção quando horário final é anterior ao inicial"
        );
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios da audiência")
    void validarDadosObrigatorios() {
        assertThrows(IllegalArgumentException.class,
            () -> new Audiencia(
                null,
                "Paulo Silva",
                LocalDateTime.of(2024, 9, 15, 10, 0),
                LocalDateTime.of(2024, 9, 15, 11, 0),
                "Sala 1"
            ),
            "Deve lançar exceção quando número do processo é nulo"
        );
    }
}
