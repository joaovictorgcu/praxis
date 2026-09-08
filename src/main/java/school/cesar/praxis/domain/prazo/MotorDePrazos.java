package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servico de dominio do subdominio <b>Prazos &amp; Agenda</b> - o motor de prazos.
 *
 * <p>Responsabilidades:
 * <ol>
 *   <li>selecionar a {@link ContagemPrazoStrategy} pelo regime de cada prazo (Strategy);</li>
 *   <li>derivar o {@link NivelAlerta} pela {@link PoliticaDeAlerta};</li>
 *   <li>emitir cada nivel <b>uma unica vez</b> por prazo (idempotencia por marco);</li>
 *   <li>publicar o evento aos observadores registrados (Observer).</li>
 * </ol>
 *
 * <p>Nao conhece persistencia, e-mail nem HTTP: recebe os prazos, devolve os
 * alertas e deixa a camada de aplicacao decidir o que fazer com eles.
 */
public class MotorDePrazos {

    private final Map<RegimeContagem, ContagemPrazoStrategy> estrategias =
            new EnumMap<>(RegimeContagem.class);
    private final PoliticaDeAlerta politica;
    private final List<ObservadorProcesso> observadores = new ArrayList<>();

    public MotorDePrazos(List<ContagemPrazoStrategy> estrategias, PoliticaDeAlerta politica) {
        for (ContagemPrazoStrategy estrategia : estrategias) {
            this.estrategias.put(estrategia.regime(), estrategia);
        }
        this.politica = politica;
    }

    public void assinar(ObservadorProcesso observador) {
        observadores.add(observador);
    }

    public ContagemPrazoStrategy estrategiaPara(RegimeContagem regime) {
        ContagemPrazoStrategy estrategia = estrategias.get(regime);
        if (estrategia == null) {
            throw new IllegalStateException("sem estrategia de contagem para o regime " + regime);
        }
        return estrategia;
    }

    /**
     * Avalia um prazo isolado. Muta o prazo apenas para registrar o marco
     * alertado, de modo que a camada de aplicacao possa persistir a mudanca.
     */
    public Optional<AlertaPrazo> avaliar(Prazo prazo, LocalDate hoje) {
        if (!prazo.estaEmAberto()) {
            return Optional.empty();
        }

        ContagemPrazoStrategy contagem = estrategiaPara(prazo.getRegime());
        boolean vencido = prazo.venceu(hoje);
        int restantes = vencido ? 0 : prazo.diasRestantes(hoje, contagem);

        Optional<NivelAlerta> nivel = vencido
                ? Optional.of(NivelAlerta.VENCE_HOJE)
                : politica.nivelPara(restantes, prazo.isFatal());
        if (nivel.isEmpty()) {
            return Optional.empty();
        }
        if (!prazo.registrarAlerta(nivel.get())) {
            return Optional.empty();
        }

        AlertaPrazo alerta = new AlertaPrazo(
                prazo.getId(),
                prazo.getNumeroProcesso().valor(),
                prazo.getDescricao(),
                prazo.getVencimento(),
                restantes,
                nivel.get(),
                vencido,
                prazo.getResponsavel());

        publicar(alerta);
        return Optional.of(alerta);
    }

    /** Varredura da agenda: avalia todos os prazos em aberto recebidos. */
    public List<AlertaPrazo> avaliarTodos(List<Prazo> prazos, LocalDate hoje) {
        List<AlertaPrazo> alertas = new ArrayList<>();
        for (Prazo prazo : prazos) {
            avaliar(prazo, hoje).ifPresent(alertas::add);
        }
        return alertas;
    }

    private void publicar(AlertaPrazo alerta) {
        EventoProcesso evento = alerta.vencido()
                ? new EventoProcesso.PrazoVencido(
                        alerta.numeroProcesso(), alerta.destinatario(),
                        alerta.descricaoPrazo(), alerta.vencimento())
                : new EventoProcesso.PrazoEmRisco(
                        alerta.numeroProcesso(), alerta.destinatario(), alerta.descricaoPrazo(),
                        alerta.vencimento(), alerta.diasRestantes(), alerta.nivel());

        for (ObservadorProcesso observador : observadores) {
            observador.notificar(evento);
        }
    }
}
