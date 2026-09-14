package school.cesar.praxis.presentation.web.seguranca;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca acao de tela reservada ao chefe (aprovar peca, remover feriado ou
 * modelo). Quem faz cumprir e o {@link SessaoInterceptor}: advogado que
 * tentar recebe 403, mesmo montando a requisicao na mao.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SomenteChefe {
}
