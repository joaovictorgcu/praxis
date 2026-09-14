package school.cesar.praxis.infrastructure.seguranca;

import org.springframework.stereotype.Component;
import school.cesar.praxis.domain.usuario.CodificadorDeSenha;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protecao de senha com PBKDF2-HMAC-SHA256 do proprio JDK: sal aleatorio por
 * usuario e iteracoes altas, sem dependencia nova. Formato guardado:
 * {@code pbkdf2$iteracoes$salBase64$hashBase64}, para permitir trocar o custo
 * depois sem invalidar as senhas ja gravadas.
 */
@Component
public class CodificadorPbkdf2 implements CodificadorDeSenha {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SAL = 16;
    private static final int TAMANHO_HASH_BITS = 256;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String codificar(String senhaEmTexto) {
        byte[] sal = new byte[TAMANHO_SAL];
        aleatorio.nextBytes(sal);
        byte[] hash = derivar(senhaEmTexto, sal, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean confere(String senhaEmTexto, String senhaCodificada) {
        if (senhaEmTexto == null || senhaCodificada == null) {
            return false;
        }
        String[] partes = senhaCodificada.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        int iteracoes = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] obtido = derivar(senhaEmTexto, sal, iteracoes);
        return MessageDigest.isEqual(esperado, obtido);
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException falha) {
            throw new IllegalStateException("PBKDF2 indisponivel nesta JVM", falha);
        }
    }
}
