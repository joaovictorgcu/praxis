package school.cesar.praxis.presentation.web.seguranca;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freio contra tentativa de senha em massa: apos N falhas seguidas para o mesmo
 * e-mail, o login daquele e-mail fica bloqueado por um intervalo. Em memoria,
 * por instancia - suficiente para o escritorio, nao para escala horizontal.
 */
@Component
public class ProtecaoForcaBruta {

    static final int MAXIMO_FALHAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(1);

    private record Tentativas(int falhas, Instant bloqueadoAte) {
    }

    private final Map<String, Tentativas> porEmail = new ConcurrentHashMap<>();

    public boolean bloqueado(String email) {
        Tentativas t = porEmail.get(chave(email));
        if (t == null || t.bloqueadoAte() == null) {
            return false;
        }
        if (Instant.now().isAfter(t.bloqueadoAte())) {
            porEmail.remove(chave(email));
            return false;
        }
        return true;
    }

    public void registrarFalha(String email) {
        porEmail.compute(chave(email), (k, atual) -> {
            int falhas = (atual == null ? 0 : atual.falhas()) + 1;
            Instant ate = falhas >= MAXIMO_FALHAS ? Instant.now().plus(BLOQUEIO) : null;
            return new Tentativas(falhas, ate);
        });
    }

    public void registrarSucesso(String email) {
        porEmail.remove(chave(email));
    }

    public long segundosRestantes(String email) {
        Tentativas t = porEmail.get(chave(email));
        if (t == null || t.bloqueadoAte() == null) {
            return 0;
        }
        return Math.max(0, Duration.between(Instant.now(), t.bloqueadoAte()).toSeconds());
    }

    private static String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
