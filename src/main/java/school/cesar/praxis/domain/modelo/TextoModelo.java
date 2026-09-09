package school.cesar.praxis.domain.modelo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value Object: um trecho de modelo, ja decomposto nos termos da linguagem.
 *
 * <p>A analise acontece uma vez, na construcao, e o texto original fica guardado
 * para poder ser persistido e reexibido no cadastro.
 */
public final class TextoModelo {

    private static final Pattern MARCADOR =
            Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");

    private final String texto;
    private final List<ExpressaoTexto> expressoes;

    public TextoModelo(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("trecho do modelo nao pode ficar vazio");
        }
        this.texto = texto;
        this.expressoes = analisar(texto);
    }

    /** Quebra o texto em literais e referencias, na ordem em que aparecem. */
    private static List<ExpressaoTexto> analisar(String texto) {
        List<ExpressaoTexto> termos = new ArrayList<>();
        Matcher marcador = MARCADOR.matcher(texto);
        int cursor = 0;

        while (marcador.find()) {
            if (marcador.start() > cursor) {
                termos.add(new Literal(texto.substring(cursor, marcador.start())));
            }
            termos.add(new ReferenciaCampo(marcador.group(1)));
            cursor = marcador.end();
        }
        if (cursor < texto.length()) {
            termos.add(new Literal(texto.substring(cursor)));
        }
        return List.copyOf(termos);
    }

    public String interpretar(ContextoTexto contexto) {
        StringBuilder saida = new StringBuilder();
        for (ExpressaoTexto termo : expressoes) {
            saida.append(termo.interpretar(contexto));
        }
        return saida.toString();
    }

    /** Campos citados no trecho, na ordem de aparicao. */
    public Set<String> placeholders() {
        Set<String> nomes = new LinkedHashSet<>();
        for (ExpressaoTexto termo : expressoes) {
            if (termo instanceof ReferenciaCampo referencia) {
                nomes.add(referencia.nome());
            }
        }
        return nomes;
    }

    public String texto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TextoModelo outro && outro.texto.equals(texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto);
    }

    @Override
    public String toString() {
        return texto;
    }
}
