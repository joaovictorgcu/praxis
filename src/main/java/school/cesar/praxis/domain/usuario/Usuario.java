package school.cesar.praxis.domain.usuario;

import java.util.Locale;

/**
 * Raiz de agregado do subdominio generico <b>Acesso</b>: quem entra no
 * sistema. Carrega a OAB porque e ela que o Proxy de segredo de justica
 * consulta - logado, o usuario nao precisa mais digitar a propria OAB a cada
 * tela: ela sai da sessao.
 *
 * <p>A senha nunca fica em texto aqui: o agregado guarda o resultado do
 * {@link CodificadorDeSenha} e so sabe responder se uma tentativa confere.
 */
public class Usuario {

    private final Long id;
    private final String nome;
    private final String email;
    private final String oab;
    private final Papel papel;
    private final String senhaCodificada;
    /** Senha dada pelo chefe (ou pela carga inicial): o sistema exige troca no primeiro acesso. */
    private final boolean senhaProvisoria;

    public Usuario(Long id, String nome, String email, String oab, Papel papel, String senhaCodificada) {
        this(id, nome, email, oab, papel, senhaCodificada, false);
    }

    public Usuario(Long id, String nome, String email, String oab, Papel papel,
                   String senhaCodificada, boolean senhaProvisoria) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do usuário é obrigatório");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("e-mail do usuário inválido: " + email);
        }
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("OAB é obrigatória");
        }
        if (papel == null) {
            throw new IllegalArgumentException("papel do usuário é obrigatório");
        }
        if (senhaCodificada == null || senhaCodificada.isBlank()) {
            throw new IllegalArgumentException("usuário sem senha");
        }
        this.id = id;
        this.nome = nome.trim();
        this.email = normalizarEmail(email);
        this.oab = oab.trim().toUpperCase(Locale.ROOT);
        this.papel = papel;
        this.senhaCodificada = senhaCodificada;
        this.senhaProvisoria = senhaProvisoria;
    }

    /** Cria o usuario ja protegendo a senha informada; a senha nasce provisoria. */
    public static Usuario novo(String nome, String email, String oab, Papel papel,
                               String senhaEmTexto, CodificadorDeSenha codificador) {
        return novo(nome, email, oab, papel, senhaEmTexto, codificador, true);
    }

    public static Usuario novo(String nome, String email, String oab, Papel papel,
                               String senhaEmTexto, CodificadorDeSenha codificador, boolean provisoria) {
        if (senhaEmTexto == null || senhaEmTexto.length() < 6) {
            throw new IllegalArgumentException("senha deve ter ao menos 6 caracteres");
        }
        return new Usuario(null, nome, email, oab, papel, codificador.codificar(senhaEmTexto), provisoria);
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Nova instancia com a senha trocada; o resto do agregado permanece. */
    public Usuario comSenha(String novaSenhaEmTexto, CodificadorDeSenha codificador) {
        if (novaSenhaEmTexto == null || novaSenhaEmTexto.length() < 6) {
            throw new IllegalArgumentException("senha deve ter ao menos 6 caracteres");
        }
        // Senha escolhida pelo proprio usuario deixa de ser provisoria.
        return new Usuario(id, nome, email, oab, papel, codificador.codificar(novaSenhaEmTexto), false);
    }

    public boolean senhaConfere(String tentativa, CodificadorDeSenha codificador) {
        return tentativa != null && codificador.confere(tentativa, senhaCodificada);
    }

    public boolean isChefe() {
        return papel == Papel.CHEFE;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getOab() { return oab; }
    public Papel getPapel() { return papel; }
    public String getSenhaCodificada() { return senhaCodificada; }
    public boolean isSenhaProvisoria() { return senhaProvisoria; }
}
