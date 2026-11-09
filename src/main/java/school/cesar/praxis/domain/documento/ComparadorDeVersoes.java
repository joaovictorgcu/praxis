package school.cesar.praxis.domain.documento;

import java.util.ArrayList;
import java.util.List;

/**
 * Servico de dominio: compara dois textos linha a linha e diz o que foi
 * mantido, incluido e removido.
 *
 * <p>Algoritmo da <b>maior subsequencia comum</b> (LCS) por programacao
 * dinamica: {@code lcs[i][j]} guarda o tamanho da maior sequencia de linhas
 * iguais entre o sufixo {@code a[i..]} e o sufixo {@code b[j..]}. A tabela e
 * preenchida de tras para frente em O(n·m) e percorrida de frente para tras:
 * linha igual avanca nos dois textos; senao, avanca no lado que preserva a
 * maior subsequencia. Em empate, a remocao vem antes da inclusao, como no
 * {@code diff} tradicional.
 */
public final class ComparadorDeVersoes {

    /** Teto da tabela (linhas de um × linhas do outro): ~4 milhoes de celulas, ~16 MB. */
    static final long LIMITE_DE_CELULAS = 4_000_000L;

    private ComparadorDeVersoes() {
    }

    public static List<LinhaComparada> comparar(String antes, String depois) {
        String[] a = linhas(antes);
        String[] b = linhas(depois);
        int n = a.length;
        int m = b.length;
        if ((long) (n + 1) * (m + 1) > LIMITE_DE_CELULAS) {
            throw new IllegalArgumentException("peças grandes demais para comparar linha a linha ("
                    + n + " × " + m + " linhas)");
        }

        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a[i].equals(b[j])
                        ? lcs[i + 1][j + 1] + 1
                        : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }

        List<LinhaComparada> resultado = new ArrayList<>(n + m);
        int i = 0;
        int j = 0;
        while (i < n && j < m) {
            if (a[i].equals(b[j])) {
                resultado.add(new LinhaComparada(LinhaComparada.Tipo.IGUAL, i + 1, j + 1, a[i]));
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                resultado.add(new LinhaComparada(LinhaComparada.Tipo.REMOVIDA, i + 1, null, a[i]));
                i++;
            } else {
                resultado.add(new LinhaComparada(LinhaComparada.Tipo.INCLUIDA, null, j + 1, b[j]));
                j++;
            }
        }
        while (i < n) {
            resultado.add(new LinhaComparada(LinhaComparada.Tipo.REMOVIDA, i + 1, null, a[i]));
            i++;
        }
        while (j < m) {
            resultado.add(new LinhaComparada(LinhaComparada.Tipo.INCLUIDA, null, j + 1, b[j]));
            j++;
        }
        return resultado;
    }

    /** Quebra em linhas aceitando \n e \r\n; texto vazio nao tem linha nenhuma. */
    static String[] linhas(String texto) {
        if (texto == null || texto.isEmpty()) {
            return new String[0];
        }
        return texto.replace("\r\n", "\n").split("\n", -1);
    }
}
