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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class ClienteHttpIT {

    @Autowired private MockMvc mvc;

    private MockHttpSession advComum() {
        return ApoioDeTesteWeb.sessaoDe(99L, "Advogado Sem Permissão", "PE00000", Papel.ADVOGADO);
    }

    @Test
    @DisplayName("API: POST /api/clientes - Deve retornar 401 para usuario nao autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API: POST /api/clientes - Deve validar payload com 400 em requisicao autenticada")
    void deveRetornar400QuandoPayloadInvalido() throws Exception {
        mvc.perform(post("/api/clientes")
                .session(advComum())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}