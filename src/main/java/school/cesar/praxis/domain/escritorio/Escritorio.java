package school.cesar.praxis.domain.escritorio;

import school.cesar.praxis.domain.usuario.CodificadorDeSenha;

import java.util.Locale;

/**
 * Raiz de agregado do subdominio generico <b>Acesso</b>: quem entra no
 * sistema e cadastra os proprios advogados. Um unico login por escritorio -
 * nao ha usuario individual dentro dele.
 *
 * <p>A senha nunca fica em texto aqui: o agregado guarda o resultado do
 * {@link CodificadorDeSenha} e so sabe responder se uma tentativa confere,
 * mesmo padrao ja usado no restante do acesso ao sistema.
 */
public class Escritorio {

    private final Long id;
    private String nome;
    private String cnpj;
    private String email;
    private String telefone;
    private String uf;
    private String comarca;
    private final String senhaCodificada;

    public Escritorio(Long id, String nome, String cnpj, String email, String telefone,
                      String uf, String comarca, String senhaCodificada) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do escritorio e obrigatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("e-mail do escritorio invalido: " + email);
        }
        if (senhaCodificada == null || senhaCodificada.isBlank()) {
            throw new IllegalArgumentException("escritorio sem senha");
        }
        this.id = id;
        this.nome = nome.trim();
        this.cnpj = normalizar(cnpj);
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.telefone = normalizar(telefone);
        this.uf = normalizar(uf);
        this.comarca = normalizar(comarca);
        this.senhaCodificada = senhaCodificada;
    }

    /** Cria o escritorio ja protegendo a senha informada. */
    public static Escritorio novo(String nome, String cnpj, String email, String telefone,
                                  String uf, String comarca, String senhaEmTexto,
                                  CodificadorDeSenha codificador) {
        if (senhaEmTexto == null || senhaEmTexto.length() < 6) {
            throw new IllegalArgumentException("senha deve ter ao menos 6 caracteres");
        }
        return new Escritorio(null, nome, cnpj, email, telefone, uf, comarca,
                codificador.codificar(senhaEmTexto));
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public boolean senhaConfere(String tentativa, CodificadorDeSenha codificador) {
        return tentativa != null && codificador.confere(tentativa, senhaCodificada);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getUf() {
        return uf;
    }

    public String getComarca() {
        return comarca;
    }

    public String getSenhaCodificada() {
        return senhaCodificada;
    }
}
