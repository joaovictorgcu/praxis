package school.cesar.praxis.presentation.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class AssetUrl {

    public String css() {
        return url("/css/praxis.css");
    }

    public String js() {
        return url("/js/praxis.js");
    }

    private String url(String path) {
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(new ClassPathResource("static" + path).getInputStream());
            String hash = hash(bytes);
            return path.replaceFirst("\\.[^.]+$", "-" + hash + "$0");
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel calcular o hash do asset " + path, e);
        }
    }

    private String hash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5 = digest.digest(bytes);
            StringBuilder builder = new StringBuilder();
            for (byte b : md5) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 indisponivel", e);
        }
    }
}
