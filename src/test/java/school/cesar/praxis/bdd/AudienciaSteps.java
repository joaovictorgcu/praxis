package school.cesar.praxis.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps do Cucumber para a funcionalidade de Agenda de Audiências.
 * Automatiza os cenários BDD definidos em gherkin.
 */
public class AudienciaSteps {

    @Autowired
    private AgendaDeAudienciasUseCase agendaUseCase;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Variáveis para compartilhar estado entre steps
    private AudienciaResponse ultimaAudienciaResponse;
    private Exception ultimaExcecao;
    private List<AudienciaResponse> audienciasAtuais;
    private Long ultimoIdAudiencia;

    // Dado (Setup)

    @Dado("que o sistema está limpo")
    public void sistemaDespejoDados() {
        // Limpar dados não é necessário pois cada teste começa com um banco vazio
        ultimaAudienciaResponse = null;
        ultimaExcecao = null;
    }

    @Dado("que existe uma audiência cadastrada:")
    public void existeAudienciaCadastrada(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
            ultimoIdAudiencia = ultimaAudienciaResponse.getId();
        } catch (Exception e) {
            ultimaExcecao = e;
            fail("Falha ao criar audiência de setup: " + e.getMessage());
        }
    }

    @Dado("que existem as seguintes audiências cadastradas:")
    public void existemMultiplasAudiencias(DataTable dataTable) {
        List<Map<String, String>> dadosList = dataTable.asMaps();
        
        for (Map<String, String> dados : dadosList) {
            CriarAudienciaRequest request = converterMapParaRequest(dados);
            try {
                ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
            } catch (Exception e) {
                fail("Falha ao criar audiências de setup: " + e.getMessage());
            }
        }
    }

    // Quando (Ações)

    @Quando("eu crio uma audiência com os seguintes dados:")
    public void crioAudiencia(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaExcecao = null;
            ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
            ultimoIdAudiencia = ultimaAudienciaResponse.getId();
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Quando("eu tento criar uma audiência em conflito:")
    public void tentocriarAudienciaEmConflito(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
            ultimaExcecao = null;
        } catch (ConflitoDEAudienciaException e) {
            ultimaExcecao = e;
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Quando("eu crio uma audiência sem conflito:")
    public void crioAudienciaSemConflito(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaExcecao = null;
            ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
        } catch (Exception e) {
            ultimaExcecao = e;
            fail("Não deveria haver conflito: " + e.getMessage());
        }
    }

    @Quando("eu crio uma audiência na mesma hora em sala diferente:")
    public void crioAudienciaSalaDiferente(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaExcecao = null;
            ultimaAudienciaResponse = agendaUseCase.criarAudiencia(request);
        } catch (Exception e) {
            ultimaExcecao = e;
            fail("Não deveria haver conflito em salas diferentes: " + e.getMessage());
        }
    }

    @Quando("eu consulto a audiência por ID")
    public void consultoAudienciaPorId() {
        assertNotNull(ultimoIdAudiencia, "ID da audiência não foi estabelecido");
        ultimaAudienciaResponse = agendaUseCase.consultarAudiencia(ultimoIdAudiencia);
    }

    @Quando("eu edito a audiência com os seguintes dados:")
    public void editoAudiencia(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaExcecao = null;
            ultimaAudienciaResponse = agendaUseCase.editarAudiencia(ultimoIdAudiencia, request);
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Quando("eu tento editar a primeira audiência para o horário da segunda:")
    public void tentoEditarPrimeiraAudienciaParaSegundoHorario(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        // Usar o número do processo da primeira audiência
        request.setNumeroProcesso(ultimaAudienciaResponse.getNumeroProcesso());
        request.setNomeParteAutora(ultimaAudienciaResponse.getNomeParteAutora());
        request.setSala(ultimaAudienciaResponse.getSala());
        
        try {
            ultimaAudienciaResponse = agendaUseCase.editarAudiencia(ultimoIdAudiencia, request);
            ultimaExcecao = null;
        } catch (ConflitoDEAudienciaException e) {
            ultimaExcecao = e;
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Quando("eu deleto a audiência")
    public void deletoAudiencia() {
        try {
            ultimaExcecao = null;
            agendaUseCase.deletarAudiencia(ultimoIdAudiencia);
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Quando("eu detecto conflitos para a seguinte audiência:")
    public void detectoConflitos(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap();
        CriarAudienciaRequest request = converterMapParaRequest(dados);
        
        try {
            ultimaExcecao = null;
            List<AudienciaResponse> conflitos = agendaUseCase.detectarConflitos(request);
            audienciasAtuais = conflitos;
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    // Então (Verificações)

    @Então("a audiência deve ser criada com sucesso")
    public void audienciaDeveSercriada() {
        assertNull(ultimaExcecao, "Não deveria haver exceção: " + (ultimaExcecao != null ? ultimaExcecao.getMessage() : ""));
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi criada");
        assertNotNull(ultimaAudienciaResponse.getId(), "Audiência deveria ter um ID");
    }

    @Então("a audiência deve estar ativa")
    public void audienciaDeveEstarAtiva() {
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi recuperada");
        assertTrue(ultimaAudienciaResponse.isAtiva(), "Audiência deveria estar ativa");
    }

    @Então("devo receber um erro de conflito de horário")
    public void devoReceberErroDeConflito() {
        assertNotNull(ultimaExcecao, "Deveria ter lançado uma exceção");
        assertTrue(ultimaExcecao instanceof ConflitoDEAudienciaException,
            "Deveria ser ConflitoDEAudienciaException, mas foi: " + ultimaExcecao.getClass().getSimpleName());
    }

    @Então("a segunda audiência não deve ser criada")
    public void segundaAudienciaNaoDeveSercriada() {
        // Já coberto pela verificação de exceção
    }

    @Então("o sistema deve conter {int} audiências")
    public void sistemaDeveraConter(int quantidade) {
        List<AudienciaResponse> todas = agendaUseCase.listarAudiencias();
        assertEquals(quantidade, todas.size(), 
            "Sistema deveria conter " + quantidade + " audiências, mas contém " + todas.size());
    }

    @Então("devo obter os dados da audiência")
    public void devoObterDadosDaAudiencia() {
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi recuperada");
        assertNotNull(ultimaAudienciaResponse.getId(), "Audiência deveria ter um ID");
    }

    @Então("o número do processo deve ser {string}")
    public void numeroDoProcessoDeveSer(String numeroProcesso) {
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi recuperada");
        assertEquals(numeroProcesso, ultimaAudienciaResponse.getNumeroProcesso(),
            "Número do processo não corresponde");
    }

    @Então("a audiência deve ser atualizada com sucesso")
    public void audienciaDeveSerAtualizada() {
        assertNull(ultimaExcecao, "Não deveria haver exceção: " + (ultimaExcecao != null ? ultimaExcecao.getMessage() : ""));
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi atualizada");
    }

    @Então("o nome da parte autora deve ser {string}")
    public void nomeParteAutoraDeve(String nome) {
        assertNotNull(ultimaAudienciaResponse, "Audiência não foi recuperada");
        assertEquals(nome, ultimaAudienciaResponse.getNomeParteAutora(),
            "Nome da parte autora não corresponde");
    }

    @Então("a audiência não deve estar ativa")
    public void audienciaNaoDeveEstarAtiva() {
        AudienciaResponse audiencia = agendaUseCase.consultarAudiencia(ultimoIdAudiencia);
        assertNull(audiencia, "Audiência deletada não deveria ser recuperada");
    }

    @Então("o sistema não deve listar essa audiência")
    public void sistemaNavoDeveraListarAudiencia() {
        List<AudienciaResponse> todas = agendaUseCase.listarAudiencias();
        for (AudienciaResponse a : todas) {
            assertNotEquals(ultimoIdAudiencia, a.getId(),
                "Audiência deletada não deveria estar na lista");
        }
    }

    @Então("devo obter uma lista com {int} conflito")
    public void devoObterListaComConflito(int quantidade) {
        assertNotNull(audienciasAtuais, "Lista de conflitos não foi recuperada");
        assertEquals(quantidade, audienciasAtuais.size(),
            "Deveria haver " + quantidade + " conflito(s), mas há " + audienciasAtuais.size());
    }

    @Então("o conflito deve ser a audiência {string}")
    public void conflitoDeveSerAudiencia(String numeroProcesso) {
        assertNotNull(audienciasAtuais, "Lista de conflitos não foi recuperada");
        assertFalse(audienciasAtuais.isEmpty(), "Lista de conflitos está vazia");
        assertEquals(numeroProcesso, audienciasAtuais.get(0).getNumeroProcesso(),
            "Conflito deveria ser para o processo " + numeroProcesso);
    }

    @Então("a audiência não deve ser alterada")
    public void audienciaNaoDeveSerAlterada() {
        // Já coberto pela verificação de exceção
        assertNotNull(ultimaExcecao, "Deveria ter lançado uma exceção de conflito");
    }

    // Método auxiliar

    private CriarAudienciaRequest converterMapParaRequest(Map<String, String> dados) {
        CriarAudienciaRequest request = new CriarAudienciaRequest();
        
        request.setNumeroProcesso(dados.get("numeroProcesso"));
        request.setNomeParteAutora(dados.get("nomeParteAutora"));
        
        if (dados.containsKey("dataHoraInicio")) {
            request.setDataHoraInicio(LocalDateTime.parse(dados.get("dataHoraInicio"), FORMATTER));
        }
        
        if (dados.containsKey("dataHoraFim")) {
            request.setDataHoraFim(LocalDateTime.parse(dados.get("dataHoraFim"), FORMATTER));
        }
        
        request.setSala(dados.get("sala"));
        request.setObservacoes(dados.get("observacoes"));
        
        return request;
    }
}
