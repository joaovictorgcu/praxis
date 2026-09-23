package school.cesar.praxis.presentation.web.seguranca;

import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

/**
 * Concilia a OAB informada num download com a da sessao.
 *
 * <p>O {@code ?oab=} continua existindo porque o download e {@code GET} aberto
 * e o Proxy precisa de uma inscricao para decidir o segredo de justica. Mas
 * para quem esta logado ele deixa de ser campo livre: informar a inscricao de
 * outro advogado e recusado, senao bastaria a um advogado do escritorio
 * descobrir uma OAB habilitada para ler autos que nao sao dele.
 *
 * <p>Sem sessao nada muda - o chamador anonimo segue sendo julgado so pela OAB
 * que informou, que e o contrato dos scripts de leitura do README.
 */
public final class OabDeLeitura {

    private OabDeLeitura() {
    }

    /** Devolve a OAB a usar no Proxy, ou recusa se ela nao for a da sessao. */
    public static String conciliar(UsuarioLogado usuario, String oabInformada) {
        if (usuario == null) {
            return oabInformada;
        }
        String daSessao = usuario.oab();
        if (daSessao == null || !daSessao.equalsIgnoreCase(oabInformada)) {
            throw new ProxyDeAcesso.AcessoNegadoException(
                    "a OAB " + oabInformada + " não é a da sessão; leia com a sua própria inscrição");
        }
        return daSessao;
    }
}
