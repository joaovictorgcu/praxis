package school.cesar.praxis.application.usecase;

import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.PeriodoSuspensao;
import school.cesar.praxis.domain.prazo.Prazo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Texto gravado no campo {@code detalhe} de cada evento do prazo. Fica num lugar
 * so para que a trilha leia igual na tela, na API e na apresentacao.
 */
final class TextoDaTrilhaDePrazo {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TextoDaTrilhaDePrazo() {
    }

    static String data(LocalDate data) {
        return data == null ? "-" : DATA.format(data);
    }

    static String abertura(Prazo prazo) {
        String texto = prazo.getRegime().descrever(prazo.getQuantidadeDias())
                + " contados da intimação de " + data(prazo.getIntimacao())
                + (prazo.isFatal() ? "; prazo fatal" : "; prazo comum");
        if (prazo.getPrerrogativa().emDobro()) {
            texto += ". Em dobro (" + prazo.getPrerrogativa().fundamento() + "): "
                    + prazo.getRegime().descrever(prazo.diasEfetivos());
        }
        return texto + ".";
    }

    static String suspensao(Prazo prazo, int saldo) {
        PeriodoSuspensao aberta = prazo.suspensaoAberta().orElseThrow();
        return "Suspenso desde " + data(aberta.inicio()) + ". Motivo: " + aberta.motivo()
                + ". Saldo congelado: " + prazo.getRegime().descrever(saldo) + ".";
    }

    static String retomada(Prazo prazo, PeriodoSuspensao encerrada, int saldoRestituido, boolean alertasReiniciados) {
        return "Retomado em " + data(encerrada.fim().plusDays(1)) + ", após "
                + encerrada.diasParado(encerrada.fim()) + " dia(s) corrido(s) parado. Saldo de "
                + prazo.getRegime().descrever(saldoRestituido) + " restituído (CPC art. 221)."
                + (alertasReiniciados ? " Régua de alertas reiniciada." : "");
    }

    static String dilacao(Prazo prazo, int dias, String fundamento, boolean alertasReiniciados) {
        return "+" + prazo.getRegime().descrever(dias) + " (total de " + prazo.getRegime().descrever(prazo.diasEfetivos())
                + "). Fundamento: " + fundamento.trim() + "."
                + (alertasReiniciados ? " Régua de alertas reiniciada." : "");
    }

    static String alerta(AlertaPrazo alerta) {
        return alerta.vencido()
                ? "Nível VENCIDO: em aberto após o vencimento de " + data(alerta.vencimento()) + "."
                : "Nível " + alerta.nivel().rotulo() + ": faltam " + alerta.diasRestantes()
                        + " dia(s) para " + data(alerta.vencimento()) + ".";
    }

    static String cumprimento(Prazo prazo) {
        return "Cumprido em " + data(prazo.getCumpridoEm()) + ".";
    }

    static String cancelamento(String motivo) {
        return "Motivo: " + motivo.trim() + ".";
    }

    static String reabertura(Prazo prazo, String motivo) {
        return "Motivo: " + motivo.trim() + ". Volta a " + prazo.getSituacao().rotulo() + ".";
    }
}
