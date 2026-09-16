package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Liga a guarda de sessao ao painel e as mutacoes da API, e permite que
 * controller receba {@link UsuarioLogado} como parametro, em vez de mexer na
 * {@code HttpSession}.
 */
@Configuration
public class ConfiguracaoWeb implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(new SessaoInterceptor()).addPathPatterns("/painel/**");
        registro.addInterceptor(new CsrfInterceptor()).addPathPatterns("/painel/**", "/sair");
        registro.addInterceptor(new SessaoApiInterceptor()).addPathPatterns("/api/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registro) {
        registro.addRedirectViewController("/", "/painel");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvedores) {
        resolvedores.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parametro) {
                return UsuarioLogado.class.equals(parametro.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer mav,
                                          NativeWebRequest requisicao, WebDataBinderFactory binder) {
                HttpServletRequest http = requisicao.getNativeRequest(HttpServletRequest.class);
                return http == null ? null : UsuarioLogado.da(http.getSession(false));
            }
        });
    }
}
