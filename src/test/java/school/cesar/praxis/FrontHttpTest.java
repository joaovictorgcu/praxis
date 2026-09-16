package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.domain.usuario.Papel;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garantias de front que nenhuma tela pode perder: atalho de teclado para o
 * conteudo, marcacao da pagina atual no menu e cache longo dos estaticos.
 * Sao invisiveis a olho nu, entao voltariam a sumir sem teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class FrontHttpTest {

    @Autowired private MockMvc mvc;

    private static MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Carla Mendes", "PE00001", Papel.CHEFE);
    }

    @Test
    @DisplayName("toda tela do painel abre com o atalho 'Ir para o conteudo' apontando para o main")
    void atalhoDeTeclado() throws Exception {
        for (String tela : new String[]{"/painel", "/painel/processos", "/painel/documentos",
                "/painel/anexos", "/painel/modelos", "/painel/feriados", "/painel/usuarios",
                "/painel/admin", "/painel/conta"}) {
            mvc.perform(get(tela).session(chefe()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"#conteudo\"")))
                    .andExpect(content().string(containsString("id=\"conteudo\"")));
        }
    }

    @Test
    @DisplayName("o menu marca a pagina atual com aria-current, e so ela")
    void paginaAtualNoMenu() throws Exception {
        mvc.perform(get("/painel/feriados").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(containsString(
                        "<a href=\"/painel/feriados\" aria-current=\"page\" class=\"ativo\">")));
    }

    @Test
    @DisplayName("CSS e JS sao servidos com cache longo")
    void estaticosComCacheLongo() throws Exception {
        mvc.perform(get("/css/praxis.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));
        mvc.perform(get("/js/praxis.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));
    }
}
