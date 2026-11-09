package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Prazos &amp; Agenda</b>.
 *
 * <p>Referencia o Processo por identidade ({@link NumeroCnj}) em vez de por
 * objeto: agregados pequenos, consistencia transacional por prazo.
 *
 * <p>O <b>vencimento original</b> e calculado na abertura e nunca muda - mudanca
 * de calendario nao move prazo ja lancado. O <b>vencimento vigente</b> so muda
 * por um fato processual registrado no proprio prazo:
 * <ul>
 *   <li>suspensao e retomada (CPC art. 221): o prazo e recontado desde a
 *       intimacao pulando os dias suspensos, o que restitui exatamente o saldo
 *       que faltava;</li>
 *   <li>dilacao judicial (CPC art. 139, VI): soma dias ao prazo, so antes de
 *       encerrado o prazo regular (paragrafo unico);</li>
 *   <li>prerrogativa de prazo em dobro (CPC arts. 180, 183, 186 e 229), escolhida
 *       na abertura.</li>
 * </ul>
 * Quando o vencimento muda, a regua de alertas recomeca: o responsavel volta a
 * ser avisado conforme a nova data se aproxima.
 */
public class Prazo {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String descricao;
    private final LocalDate intimacao;
    private final int quantidadeDias;
    private final RegimeContagem regime;
    private final PrerrogativaPrazo prerrogativa;
    private final LocalDate vencimentoOriginal;
    private LocalDate vencimento;
    private final boolean fatal;
    private final Advogado responsavel;
    private final Set<NivelAlerta> alertasEmitidos;
    private final List<PeriodoSuspensao> suspensoes;
    private int diasAcrescidos;
    private SituacaoPrazo situacao;
    private LocalDate cumpridoEm;

    /** Construcao de um prazo novo e simples: o vencimento e derivado da Strategy. */
    public Prazo(NumeroCnj numeroProcesso,
                 String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 boolean fatal,
                 Advogado responsavel,
                 ContagemPrazoStrategy contagem) {
        this(numeroProcesso, descricao, intimacao, quantidadeDias, fatal, responsavel,
                PrerrogativaPrazo.NENHUMA, contagem);
    }

    /** Construcao de um prazo novo, com ou sem prerrogativa de prazo em dobro. */
    public Prazo(NumeroCnj numeroProcesso,
                 String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 boolean fatal,
                 Advogado responsavel,
                 PrerrogativaPrazo prerrogativa,
                 ContagemPrazoStrategy contagem) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("prazo exige processo");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descrição do prazo é obrigatória");
        }
        if (intimacao == null) {
            throw new IllegalArgumentException("data de intimação é obrigatória");
        }
        if (quantidadeDias <= 0) {
            throw new IllegalArgumentException("prazo deve ter ao menos 1 dia");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("prazo exige advogado responsável");
        }
        if (contagem == null) {
            throw new IllegalArgumentException("prazo exige o regime de contagem");
        }
        this.id = null;
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao.trim();
        this.intimacao = intimacao;
        this.quantidadeDias = quantidadeDias;
        this.regime = contagem.regime();
        this.prerrogativa = prerrogativa == null ? PrerrogativaPrazo.NENHUMA : prerrogativa;
        this.fatal = fatal;
        this.responsavel = responsavel;
        this.alertasEmitidos = EnumSet.noneOf(NivelAlerta.class);
        this.suspensoes = new ArrayList<>();
        this.diasAcrescidos = 0;
        this.situacao = SituacaoPrazo.EM_ABERTO;
        this.vencimento = contagem.calcularVencimento(intimacao, diasEfetivos());
        this.vencimentoOriginal = this.vencimento;
    }

    /** Reconstituicao a partir da persistencia (nao recalcula vencimento). */
    public Prazo(Long id,
                 NumeroCnj numeroProcesso,
                 String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 RegimeContagem regime,
                 PrerrogativaPrazo prerrogativa,
                 LocalDate vencimentoOriginal,
                 LocalDate vencimento,
                 boolean fatal,
                 Advogado responsavel,
                 Set<NivelAlerta> alertasEmitidos,
                 SituacaoPrazo situacao,
                 LocalDate cumpridoEm,
                 int diasAcrescidos,
                 List<PeriodoSuspensao> suspensoes) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao;
        this.intimacao = intimacao;
        this.quantidadeDias = quantidadeDias;
        this.regime = regime;
        this.prerrogativa = prerrogativa == null ? PrerrogativaPrazo.NENHUMA : prerrogativa;
        this.vencimento = vencimento;
        this.vencimentoOriginal = vencimentoOriginal == null ? vencimento : vencimentoOriginal;
        this.fatal = fatal;
        this.responsavel = responsavel;
        this.alertasEmitidos = alertasEmitidos == null || alertasEmitidos.isEmpty()
                ? EnumSet.noneOf(NivelAlerta.class)
                : EnumSet.copyOf(alertasEmitidos);
        this.situacao = situacao == null ? SituacaoPrazo.EM_ABERTO : situacao;
        this.cumpridoEm = cumpridoEm;
        this.diasAcrescidos = diasAcrescidos;
        this.suspensoes = suspensoes == null ? new ArrayList<>() : new ArrayList<>(suspensoes);
    }

    // --- Contagem ---

    /** Dias que o prazo tem de verdade: o informado, dobrado pela prerrogativa, mais a dilacao. */
    public int diasEfetivos() {
        return quantidadeDias * prerrogativa.multiplicador() + diasAcrescidos;
    }

    /**
     * Dias contaveis que faltam. Suspenso, o prazo nao corre: devolve o saldo
     * congelado no inicio da suspensao, qualquer que seja {@code hoje}.
     */
    public int diasRestantes(LocalDate hoje, ContagemPrazoStrategy contagem) {
        if (situacao == SituacaoPrazo.SUSPENSO) {
            return saldoNaSuspensao(contagem);
        }
        return contagem.diasRestantes(hoje, vencimento, this::diaSuspenso);
    }

    /**
     * Saldo que faltava quando a suspensao aberta comecou: dias contaveis do
     * inicio da suspensao ate o vencimento vigente, limitado ao prazo inteiro
     * (suspensao no proprio dia da intimacao devolve o prazo todo).
     */
    public int saldoNaSuspensao(ContagemPrazoStrategy contagem) {
        PeriodoSuspensao aberta = suspensaoAberta()
                .orElseThrow(() -> new IllegalStateException("o prazo não está suspenso"));
        int saldo = contagem.diasRestantes(aberta.inicio().minusDays(1), vencimento,
                dia -> suspensoes.stream().filter(s -> !s.emAberto()).anyMatch(s -> s.contem(dia)));
        return Math.min(saldo, diasEfetivos());
    }

    /** Suspenso o prazo nao vence: so o prazo que esta correndo pode ter passado da data. */
    public boolean venceu(LocalDate hoje) {
        return situacao != SituacaoPrazo.SUSPENSO && hoje.isAfter(vencimento);
    }

    /** So o prazo em aberto corre e entra na varredura. */
    public boolean estaEmAberto() {
        return situacao == SituacaoPrazo.EM_ABERTO;
    }

    public boolean diaSuspenso(LocalDate dia) {
        return suspensoes.stream().anyMatch(periodo -> periodo.contem(dia));
    }

    public Optional<PeriodoSuspensao> suspensaoAberta() {
        return suspensoes.isEmpty() || !suspensoes.get(suspensoes.size() - 1).emAberto()
                ? Optional.empty()
                : Optional.of(suspensoes.get(suspensoes.size() - 1));
    }

    // --- Alertas ---

    /**
     * Marca o nivel como ja alertado. Retorna {@code false} quando o nivel ja
     * havia sido emitido - e assim que o motor evita reenviar o mesmo alerta a
     * cada varredura do dia.
     */
    public boolean registrarAlerta(NivelAlerta nivel) {
        return alertasEmitidos.add(nivel);
    }

    public boolean jaAlertou(NivelAlerta nivel) {
        return alertasEmitidos.contains(nivel);
    }

    // --- Ciclo de vida ---

    /**
     * Suspende o prazo a partir de {@code inicio} (inclusive): dali em diante
     * nenhum dia conta ate a retomada. Exige motivo - a hipotese do art. 221 ou
     * do art. 313 do CPC.
     */
    public void suspender(LocalDate inicio, String motivo, LocalDate hoje) {
        exigirSituacao("suspender", SituacaoPrazo.EM_ABERTO);
        exigirTexto(motivo, "a suspensão exige motivo (CPC art. 221 ou hipótese do art. 313)");
        if (inicio == null) {
            throw new IllegalArgumentException("informe a data de início da suspensão");
        }
        if (inicio.isBefore(intimacao)) {
            throw new IllegalArgumentException(
                    "a suspensão não pode começar antes da intimação (" + intimacao + ")");
        }
        if (inicio.isAfter(hoje)) {
            throw new IllegalArgumentException(
                    "a suspensão não pode começar no futuro: registre-a quando ela ocorrer");
        }
        if (inicio.isAfter(vencimento)) {
            throw new IllegalStateException(
                    "o prazo já havia vencido em " + vencimento + ": não há saldo a suspender");
        }
        if (!suspensoes.isEmpty()) {
            PeriodoSuspensao anterior = suspensoes.get(suspensoes.size() - 1);
            if (anterior.emAberto()) {
                throw new IllegalStateException("o prazo já tem uma suspensão aberta desde " + anterior.inicio());
            }
            if (!inicio.isAfter(anterior.fim())) {
                throw new IllegalArgumentException(
                        "a suspensão se sobrepõe à anterior, encerrada em " + anterior.fim());
            }
        }
        suspensoes.add(PeriodoSuspensao.aberta(inicio, motivo));
        situacao = SituacaoPrazo.SUSPENSO;
    }

    /**
     * Retoma o prazo em {@code retomada}: a suspensao fecha na vespera e o prazo
     * e recontado. O resultado restitui "tempo igual ao que faltava para sua
     * complementacao" (CPC art. 221).
     *
     * @return {@code true} se o vencimento mudou (e a regua de alertas recomecou)
     */
    public boolean retomar(LocalDate retomada, LocalDate hoje, ContagemPrazoStrategy contagem) {
        exigirSituacao("retomar", SituacaoPrazo.SUSPENSO);
        PeriodoSuspensao aberta = suspensaoAberta()
                .orElseThrow(() -> new IllegalStateException("o prazo não tem suspensão aberta"));
        if (retomada == null) {
            throw new IllegalArgumentException("informe a data da retomada");
        }
        if (!retomada.isAfter(aberta.inicio())) {
            throw new IllegalArgumentException(
                    "a retomada deve ser posterior ao início da suspensão (" + aberta.inicio() + ")");
        }
        if (retomada.isAfter(hoje)) {
            throw new IllegalArgumentException(
                    "a retomada não pode ser futura: registre-a quando o prazo voltar a correr");
        }
        suspensoes.set(suspensoes.size() - 1, aberta.encerradaEm(retomada.minusDays(1)));
        situacao = SituacaoPrazo.EM_ABERTO;
        return recalcular(contagem);
    }

    /**
     * Dilacao judicial (CPC art. 139, VI): soma {@code dias} ao prazo. O
     * paragrafo unico so admite a dilacao antes de encerrado o prazo regular.
     *
     * @return {@code true} se o vencimento mudou (e a regua de alertas recomecou)
     */
    public boolean dilatar(int dias, String fundamento, LocalDate hoje, ContagemPrazoStrategy contagem) {
        exigirSituacao("dilatar", SituacaoPrazo.EM_ABERTO);
        if (dias <= 0) {
            throw new IllegalArgumentException("a dilação deve acrescentar ao menos 1 dia");
        }
        if (dias > 365) {
            throw new IllegalArgumentException("dilação acima de 365 dias não é aceita");
        }
        exigirTexto(fundamento, "a dilação exige o fundamento (a decisão que dilatou o prazo, CPC art. 139, VI)");
        if (hoje.isAfter(vencimento)) {
            throw new IllegalStateException("CPC art. 139, parágrafo único: a dilação só cabe antes de encerrado"
                    + " o prazo regular, que venceu em " + vencimento);
        }
        diasAcrescidos += dias;
        return recalcular(contagem);
    }

    /** Durante a suspensao e vedado praticar ato processual (CPC art. 314): so cumpre prazo em aberto. */
    public void cumprir(LocalDate quando) {
        if (situacao == SituacaoPrazo.CUMPRIDO) {
            throw new IllegalStateException("prazo já cumprido em " + cumpridoEm);
        }
        if (situacao == SituacaoPrazo.SUSPENSO) {
            throw new IllegalStateException(
                    "prazo suspenso: retome-o antes de cumprir (CPC art. 314 veda ato durante a suspensão)");
        }
        exigirSituacao("cumprir", SituacaoPrazo.EM_ABERTO);
        this.situacao = SituacaoPrazo.CUMPRIDO;
        this.cumpridoEm = quando;
    }

    /** Prazo lancado por engano ou que perdeu o objeto. Nao apaga nada: o prazo sai da agenda com motivo. */
    public void cancelar(String motivo) {
        exigirSituacao("cancelar", SituacaoPrazo.EM_ABERTO, SituacaoPrazo.SUSPENSO);
        exigirTexto(motivo, "o cancelamento exige motivo");
        this.situacao = SituacaoPrazo.CANCELADO;
    }

    /**
     * Desfaz um encerramento (cumprimento registrado por engano, cancelamento
     * indevido). Volta a suspenso se havia suspensao aberta quando foi cancelado.
     */
    public void reabrir(String motivo) {
        exigirSituacao("reabrir", SituacaoPrazo.CUMPRIDO, SituacaoPrazo.CANCELADO);
        exigirTexto(motivo, "a reabertura exige motivo");
        this.situacao = suspensaoAberta().isPresent() ? SituacaoPrazo.SUSPENSO : SituacaoPrazo.EM_ABERTO;
        this.cumpridoEm = null;
    }

    /**
     * Reconta o prazo desde a intimacao com os dias efetivos e as suspensoes
     * encerradas. Se a data mudou, a regua de alertas recomeca do zero.
     */
    private boolean recalcular(ContagemPrazoStrategy contagem) {
        if (contagem.regime() != regime) {
            throw new IllegalArgumentException("a contagem informada não é a do regime do prazo (" + regime + ")");
        }
        LocalDate novo = contagem.calcularVencimento(intimacao, diasEfetivos(), this::diaSuspenso);
        if (novo.equals(vencimento)) {
            return false;
        }
        vencimento = novo;
        alertasEmitidos.clear();
        return true;
    }

    private void exigirSituacao(String operacao, SituacaoPrazo... permitidas) {
        for (SituacaoPrazo permitida : permitidas) {
            if (situacao == permitida) {
                return;
            }
        }
        throw new IllegalStateException("não é possível " + operacao + " um prazo " + situacao.rotulo());
    }

    private static void exigirTexto(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    // --- Leitura ---

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getIntimacao() {
        return intimacao;
    }

    public int getQuantidadeDias() {
        return quantidadeDias;
    }

    public RegimeContagem getRegime() {
        return regime;
    }

    public PrerrogativaPrazo getPrerrogativa() {
        return prerrogativa;
    }

    public LocalDate getVencimentoOriginal() {
        return vencimentoOriginal;
    }

    /** Vencimento vigente: o original ajustado por suspensao, retomada e dilacao. */
    public LocalDate getVencimento() {
        return vencimento;
    }

    public boolean isFatal() {
        return fatal;
    }

    public Advogado getResponsavel() {
        return responsavel;
    }

    public SituacaoPrazo getSituacao() {
        return situacao;
    }

    public boolean isCumprido() {
        return situacao == SituacaoPrazo.CUMPRIDO;
    }

    public LocalDate getCumpridoEm() {
        return cumpridoEm;
    }

    public int getDiasAcrescidos() {
        return diasAcrescidos;
    }

    public List<PeriodoSuspensao> getSuspensoes() {
        return Collections.unmodifiableList(suspensoes);
    }

    public Set<NivelAlerta> getAlertasEmitidos() {
        return Collections.unmodifiableSet(alertasEmitidos);
    }
}
