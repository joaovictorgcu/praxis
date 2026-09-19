package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.presentation.web.seguranca.CsrfInterceptor;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AudienciaHttpIT {

    @Autowired private MockMvc mvc;

    private MockHttpSession advComum() {
        return ApoioDeTesteWeb.sessaoDe(99L, "Advogado Sem Permissao", "PE00000", Papel.ADVOGADO);
    }

    private MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Carla Mendes", "PE00001", Papel.CHEFE);
    }

    @Test
    @DisplayName("API: POST /api/audiencias - Deve retornar 401 para usuario nao autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mvc.perform(post("/api/audiencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API: POST /api/audiencias - Deve validar payload com 400 em requisicao autenticada")
    void deveRetornar400QuandoPayloadInvalido() throws Exception {
        mvc.perform(post("/api/audiencias")
                .session(advComum())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/audiencias com fim antes do inicio → 400 {erro}")
    void horarioInvertidoVira400ComErro() throws Exception {
        mvc.perform(post("/api/audiencias").session(chefe())
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroProcesso": "0001111-11.2026.8.17.0011",
                                  "nomeParteAutora": "Ana",
                                  "dataHoraInicio": "2026-09-15T15:00:00",
                                  "dataHoraFim": "2026-09-15T14:00:00",
                                  "sala": "Sala 1"
                                }"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro", containsString("posterior")));
    }

    @Test
    @DisplayName("POST /api/audiencias em sala/horario ocupados → 409 {erro}")
    void conflitoVira409ComErro() throws Exception {
        String primeira = """
                {
                  "numeroProcesso": "0002222-22.2026.8.17.0022",
                  "nomeParteAutora": "Bruno",
                  "dataHoraInicio": "2026-09-15T10:00:00",
                  "dataHoraFim": "2026-09-15T11:00:00",
                  "sala": "Sala 1"
                }""";
        mvc.perform(post("/api/audiencias").session(chefe())
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(primeira))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/audiencias").session(chefe())
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroProcesso": "0003333-33.2026.8.17.0033",
                                  "nomeParteAutora": "Carla",
                                  "dataHoraInicio": "2026-09-15T10:30:00",
                                  "dataHoraFim": "2026-09-15T11:30:00",
                                  "sala": "Sala 1"
                                }"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", containsString("Conflito")));
    }
}
