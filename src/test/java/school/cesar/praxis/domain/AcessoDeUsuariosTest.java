package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.usuario.CodificadorDeSenha;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.infrastructure.seguranca.CodificadorPbkdf2;

import static org.junit.jupiter.api.Assertions.*;

/** Regras do agregado Usuario e do codificador de senha, sem Spring. */
class AcessoDeUsuariosTest {

    private final CodificadorDeSenha codificador = new CodificadorPbkdf2();

    @Test
    @DisplayName("usuario novo guarda a senha codificada e confere so a senha certa")
    void senhaProtegida() {
        Usuario ana = Usuario.novo("Ana Souza", "Ana@praxis.adv.br", "pe12345",
                Papel.ADVOGADO, "segredo1", codificador);

        assertNotEquals("segredo1", ana.getSenhaCodificada());
        assertTrue(ana.getSenhaCodificada().startsWith("pbkdf2$"));
        assertTrue(ana.senhaConfere("segredo1", codificador));
        assertFalse(ana.senhaConfere("segredo2", codificador));
        assertFalse(ana.senhaConfere(null, codificador));
    }

    @Test
    @DisplayName("e-mail e OAB sao normalizados para a busca nao depender de caixa")
    void normalizacao() {
        Usuario ana = Usuario.novo("Ana", "  Ana.Souza@Praxis.adv.br ", " pe12345 ",
                Papel.ADVOGADO, "segredo1", codificador);
        assertEquals("ana.souza@praxis.adv.br", ana.getEmail());
        assertEquals("PE12345", ana.getOab());
    }

    @Test
    @DisplayName("mesma senha gera hashes diferentes (sal aleatorio) e ambos conferem")
    void salAleatorio() {
        String a = codificador.codificar("praxis123");
        String b = codificador.codificar("praxis123");
        assertNotEquals(a, b);
        assertTrue(codificador.confere("praxis123", a));
        assertTrue(codificador.confere("praxis123", b));
        assertFalse(codificador.confere("praxis123", "texto-que-nao-e-hash"));
    }

    @Test
    @DisplayName("so o chefe aprova peca e administra cadastros")
    void prerrogativasDoPapel() {
        assertTrue(Papel.CHEFE.podeAprovarPeca());
        assertTrue(Papel.CHEFE.podeAdministrarCadastros());
        assertFalse(Papel.ADVOGADO.podeAprovarPeca());
        assertFalse(Papel.ADVOGADO.podeAdministrarCadastros());
    }

    @Test
    @DisplayName("invariantes: senha curta, e-mail sem arroba e OAB vazia sao recusados")
    void invariantes() {
        assertThrows(IllegalArgumentException.class, () ->
                Usuario.novo("Ana", "ana@praxis.adv.br", "PE1", Papel.ADVOGADO, "12345", codificador));
        assertThrows(IllegalArgumentException.class, () ->
                Usuario.novo("Ana", "ana.praxis.adv.br", "PE1", Papel.ADVOGADO, "segredo1", codificador));
        assertThrows(IllegalArgumentException.class, () ->
                Usuario.novo("Ana", "ana@praxis.adv.br", " ", Papel.ADVOGADO, "segredo1", codificador));
        assertThrows(IllegalArgumentException.class, () ->
                Usuario.novo("Ana", "ana@praxis.adv.br", "PE1", null, "segredo1", codificador));
    }
}
