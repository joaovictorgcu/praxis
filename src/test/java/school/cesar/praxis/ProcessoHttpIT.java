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
import school.cesar.praxis.domain.usuario.Papel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class ProcessoHttpIT {

    @Autowired private MockMvc mvc;

    private MockHttpSession advComum() {
        return ApoioDeTesteWeb.sessaoDe(99L, "Advogado Sem Permissão", "PE00000", Papel.ADVOGADO);
    }

    @Test
    @DisplayName("API: POST /api/processos - Deve retornar 401 para usuario nao autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mvc.perform(post("/api/processos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API: POST /api/processos - Deve retornar 400 para payload invalido em requisicao autenticada")
    void deveRetornarErroQuandoPayloadInvalido() throws Exception {
        mvc.perform(post("/api/processos")
                .session(advComum())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}