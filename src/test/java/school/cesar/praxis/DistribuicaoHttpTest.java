package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class DistribuicaoHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuariosUseCases.Autenticar autenticar;

    private MockHttpSession chefe() {
        Usuario carla = autenticar.executar(new UsuariosUseCases.Autenticar.Comando("carla.mendes@praxis.adv.br", "praxis123"));
        return ApoioDeTesteWeb.sessaoDe(carla.getId(), carla.getNome(), carla.getOab(), Papel.CHEFE);
    }

    @Test
    @DisplayName("API: POST /api/distribuicao - Deve responder com sucesso ao solicitar distribuição")
    void deveDistribuirProcessoViaHttp() throws Exception {
        String jsonPayload = """
            {
                "numeroProcesso": "0001111-22.2026.8.17.0001",
                "areaDireito": "Trabalhista",
                "candidatos": [
                    {
                        "nome": "Ana Souza",
                        "email": "ana@praxis.adv.br",
                        "oab": "PE12345",
                        "especialidade": "Trabalhista",
                        "processosAtivos": 2,
                        "disponivel": true
                    }
                ]
            }
            """;

        mvc.perform(post("/api/distribuicao")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk());
    }
}