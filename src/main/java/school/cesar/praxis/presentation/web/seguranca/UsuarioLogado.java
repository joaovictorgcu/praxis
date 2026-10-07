package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpSession;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import java.io.Serializable;

/**
 * Projecao de quem esta logado, guardada na sessao HTTP. Nao leva a senha
 * codificada para a sessao: so o que as telas precisam - nome, OAB (para o
 * Proxy de segredo de justica) e papel (para o que aparece e o que e
 * permitido).
 *
 * <p>Hoje quem loga e o {@code Escritorio} (login unico, sem usuario
 * individual dentro dele): {@link #de(Escritorio)} projeta o escritorio
 * autenticado nesta mesma forma, como chefe (autoridade maxima) e sem OAB
 * propria - o escritorio nao tem uma, so os advogados que ele cadastra.
 */
public record UsuarioLogado(Long id, String nome, String email, String oab, Papel papel,
                            boolean senhaProvisoria)
        implements Serializable {

    public static final String CHAVE_SESSAO = "usuarioLogado";

    /** Sessao com senha definitiva (uso em testes e na maioria dos fluxos). */
    public UsuarioLogado(Long id, String nome, String email, String oab, Papel papel) {
        this(id, nome, email, oab, papel, false);
    }

    public static UsuarioLogado de(Usuario usuario) {
        return new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getOab(), usuario.getPapel(), usuario.isSenhaProvisoria());
    }

    /** Escritorio logado: autoridade maxima (chefe), sem OAB propria. */
    public static UsuarioLogado de(Escritorio escritorio) {
        return new UsuarioLogado(escritorio.getId(), escritorio.getNome(), escritorio.getEmail(),
                "", Papel.CHEFE);
    }

    public static UsuarioLogado da(HttpSession sessao) {
        return sessao == null ? null : (UsuarioLogado) sessao.getAttribute(CHAVE_SESSAO);
    }

    public boolean chefe() {
        return papel == Papel.CHEFE;
    }

    public String rotuloPapel() {
        return papel.rotulo();
    }
}
