package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Texto de tela para as constantes que o dominio guarda sem acento nem espaco
 * (EM_REVISAO, INTIMACAO, POR_HORA). Fica na apresentacao porque e so rotulo:
 * a regra continua comparando a constante. Nos templates: {@code ${@rotulos.de(x)}}.
 */
@Component("rotulos")
public class Rotulos {

    private static final Map<String, String> TEXTO = Map.ofEntries(
            // Estado da peca no fluxo de aprovacao.
            Map.entry("RASCUNHO", "rascunho"),
            Map.entry("EM_REVISAO", "em revisão"),
            Map.entry("APROVADO", "aprovado"),
            Map.entry("REJEITADO", "rejeitado"),
            Map.entry("PROTOCOLADO", "protocolado"),
            // Tipo de andamento do processo.
            Map.entry("INTIMACAO", "intimação"),
            Map.entry("CITACAO", "citação"),
            Map.entry("AUDIENCIA", "audiência"),
            Map.entry("DESPACHO", "despacho"),
            Map.entry("SENTENCA", "sentença"),
            Map.entry("JUNTADA", "juntada"),
            Map.entry("OUTRO", "outro"),
            // Modalidade de honorario.
            Map.entry("FIXO", "fixo"),
            Map.entry("POR_HORA", "por hora"),
            Map.entry("QUOTA_LITIS", "quota litis"),
            // Historico do contrato de honorario.
            Map.entry("CONTRATACAO", "contratação"),
            // Abrangencia do feriado.
            Map.entry("NACIONAL", "nacional"),
            Map.entry("ESTADUAL", "estadual"),
            Map.entry("COMARCAL", "comarcal"));

    /** Rotulo em minusculas; quem quiser a inicial maiuscula usa {@link #titulo(Object)}. */
    public String de(Object valor) {
        if (valor == null) {
            return "";
        }
        String nome = valor instanceof Enum<?> constante ? constante.name() : valor.toString();
        return TEXTO.getOrDefault(nome, nome.toLowerCase().replace('_', ' '));
    }

    public String titulo(Object valor) {
        String texto = de(valor);
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** "20.00" -> "20"; "12.50" -> "12,5": sem zero a mais, sem notação científica. */
    public String percentual(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        BigDecimal limpo = valor.stripTrailingZeros();
        if (limpo.scale() < 0) {
            limpo = limpo.setScale(0);
        }
        return limpo.toPlainString().replace('.', ',');
    }
}
