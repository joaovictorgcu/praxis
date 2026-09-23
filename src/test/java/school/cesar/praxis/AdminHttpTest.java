package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.AdministracaoUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tela de administracao: so o chefe entra, o panorama traz todo cadastro
 * (inclusive prazo ja cumprido, que a agenda do dia esconde) e a senha
 * codificada nunca chega ao HTML.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class AdminHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuariosUseCases.Autenticar autenticar;
    @Autowired private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired private PrazosUseCases.AbrirPrazo abrirPrazo;
    @Autowired private PrazosUseCases.CumprirPrazo cumprirPrazo;
    @Autowired private AdministracaoUseCases.ConsultarPanorama panorama;

    private MockHttpSession chefe() {
        Usuario carla = autenticar.executar(
                new UsuariosUseCases.Autenticar.Comando("carla.mendes@praxis.adv.br", "praxis123"));
        return ApoioDeTesteWeb.sessaoDe(carla.getId(), carla.getNome(), carla.getOab(), Papel.CHEFE);
    }

    private MockHttpSession advogada() {
        Usuario ana = autenticar.executar(
                new UsuariosUseCases.Autenticar.Comando("ana.souza@praxis.adv.br", "praxis123"));
        return ApoioDeTesteWeb.sessaoDe(ana.getId(), ana.getNome(), ana.getOab(), Papel.ADVOGADO);
    }

    @Test
    @DisplayName("advogado nao entra na administracao; sem sessao cai no login")
    void reservadaAoChefe() throws Exception {
        mvc.perform(get("/painel/admin").session(advogada()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/painel/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("chefe ve o panorama com os cadastros e a ficha da instancia")
    void panoramaNaTela() throws Exception {
        mvc.perform(get("/painel/admin").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Instância em Execução")))
                .andExpect(content().string(containsString("carla.mendes@praxis.adv.br")))
                .andExpect(content().string(containsString("Contratos de Honorário")))
                .andExpect(content().string(containsString("Partes Contrárias")));
    }

    @Test
    @DisplayName("senha codificada nunca vai para o HTML da administracao")
    void semSenhaNoHtml() throws Exception {
        Usuario carla = autenticar.executar(
                new UsuariosUseCases.Autenticar.Comando("carla.mendes@praxis.adv.br", "praxis123"));
        assertTrue(carla.getSenhaCodificada().length() > 10, "senha codificada vazia invalidaria o teste");

        mvc.perform(get("/painel/admin").session(chefe()))
                .andExpect(content().string(not(containsString(carla.getSenhaCodificada()))))
                .andExpect(content().string(not(containsString("praxis123"))));
    }

    @Test
    @DisplayName("panorama lista prazo ja cumprido, que a agenda do dia deixa de fora")
    void incluiPrazoCumprido() throws Exception {
        String numero = "0000111-22.2026.8.17.0001";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente do panorama", "Recife", false,
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345"));
        var prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                numero, "Contestacao do panorama", LocalDate.now().minusDays(1), 15, true,
                RegimeContagem.DIAS_UTEIS));
        cumprirPrazo.executar(prazo.getId());

        assertTrue(panorama.executar().prazos().stream()
                        .anyMatch(item -> item.prazoId().equals(prazo.getId()) && item.cumprido()),
                "prazo cumprido deveria aparecer no panorama");

        mvc.perform(get("/painel/admin").session(chefe()))
                .andExpect(content().string(containsString("Contestacao do panorama")));
    }
}
