package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicacao com o perfil {@code prod} - Flyway criando o esquema, o
 * Hibernate so validando - e confere o que a instancia publicada promete: o
 * administrador da demonstracao entra com a senha configurada, sem cair na
 * troca de senha, e o escritorio de exemplo esta montado.
 *
 * <p>O banco e H2 em modo PostgreSQL: prova a configuracao do perfil e o
 * acordo entre migracao e entidades, nao a sintaxe do PostgreSQL real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Import(ApoioDeTesteWeb.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:praxis-prod;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        // Cookie Secure recusaria a sessao no MockMvc, que fala http.
        "server.servlet.session.cookie.secure=false",
        // O perfil prod nao traz senha de administrador embutida (viria publicada
        // no repositorio): quem implanta injeta PRAXIS_ADMIN_SENHA. O teste faz o
        // papel do ambiente e define a sua.
        "praxis.admin.senha=" + ImplantacaoHttpTest.SENHA_ADMIN
})
class ImplantacaoHttpTest {

    static final String SENHA_ADMIN = "senha-do-teste-de-implantacao";

    @Autowired private MockMvc mvc;
    @Autowired private PrazosUseCases.ConsultarAgenda agenda;

    @Test
    @DisplayName("o administrador da demonstracao entra com admin@admin e vai direto ao painel")
    void loginDaDemonstracao() throws Exception {
        MvcResult resultado = mvc.perform(post("/login")
                        .param("email", "admin@admin")
                        .param("senha", SENHA_ADMIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painel"))
                .andReturn();

        HttpSession sessao = resultado.getRequest().getSession(false);
        assertNotNull(sessao, "login deveria abrir sessão");
        UsuarioLogado logado = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.CHAVE_SESSAO);
        assertNotNull(logado);
        assertTrue(logado.chefe(), "o administrador e chefe");
        assertFalse(logado.senhaProvisoria(), "a demonstracao não cai na troca de senha");
    }

    @Test
    @DisplayName("senha errada do administrador nao entra")
    void senhaErrada() throws Exception {
        mvc.perform(post("/login").param("email", "admin@admin").param("senha", SENHA_ADMIN + "-errada"))
                .andExpect(status().isOk())   // volta ao formulario com a mensagem generica
                .andReturn();
    }

    @Test
    @DisplayName("a instancia publicada sobe com a carteira de exemplo montada")
    void dadosDeExemplo() {
        var emAberto = agenda.executar(LocalDate.now().plusMonths(3));

        assertTrue(emAberto.size() >= 8,
                "a carteira de demonstracao tem varios prazos em aberto, e não só os do nucleo");
        assertTrue(emAberto.stream().anyMatch(PrazosUseCases.ConsultarAgenda.ItemAgenda::vencido),
                "um prazo vencido em aberto, para a agenda mostrar o alerta de prazo perdido");
        assertTrue(emAberto.stream().anyMatch(PrazosUseCases.ConsultarAgenda.ItemAgenda::fatal),
                "prazos fatais, que são os que disparam alerta");
        assertTrue(emAberto.stream().map(PrazosUseCases.ConsultarAgenda.ItemAgenda::numeroProcesso)
                        .distinct().count() >= 5,
                "prazos espalhados por varios processos");
    }
}
