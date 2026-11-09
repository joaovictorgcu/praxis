package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.HistoricoDePrazoRepositorio;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.PeriodoSuspensao;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do ciclo de vida do prazo depois de aberto: suspender, retomar,
 * dilatar, cancelar, reabrir e consultar a ficha.
 *
 * <p>Cada operacao segue o mesmo roteiro, numa transacao so: carrega o agregado,
 * deixa o dominio validar e recalcular, grava o prazo e acrescenta o evento a
 * trilha com o vencimento de antes e o de depois. Se qualquer passo falha, nada
 * fica gravado - nem o prazo alterado, nem um evento solto.
 */
@Service
public class CicloDeVidaPrazoService implements
        PrazosUseCases.SuspenderPrazo,
        PrazosUseCases.RetomarPrazo,
        PrazosUseCases.DilatarPrazo,
        PrazosUseCases.CancelarPrazo,
        PrazosUseCases.ReabrirPrazo,
        PrazosUseCases.ConsultarPrazo {

    private final PrazoRepositorio prazos;
    private final HistoricoDePrazoRepositorio historico;
    private final MotorDePrazos motor;
    private final Relogio relogio;

    public CicloDeVidaPrazoService(PrazoRepositorio prazos,
                                   HistoricoDePrazoRepositorio historico,
                                   MotorDePrazos motor,
                                   Relogio relogio) {
        this.prazos = prazos;
        this.historico = historico;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Prazo executar(PrazosUseCases.SuspenderPrazo.Comando comando) {
        Prazo prazo = carregar(comando.prazoId());
        LocalDate antes = prazo.getVencimento();
        prazo.suspender(comando.inicio(), comando.motivo(), relogio.hoje());
        int saldo = prazo.saldoNaSuspensao(contagemDe(prazo));
        Prazo salvo = prazos.salvar(prazo);
        registrar(salvo, TipoEventoPrazo.SUSPENSAO, antes, salvo.getVencimento(),
                TextoDaTrilhaDePrazo.suspensao(salvo, saldo), comando.autor());
        return salvo;
    }

    @Override
    @Transactional
    public Prazo executar(PrazosUseCases.RetomarPrazo.Comando comando) {
        Prazo prazo = carregar(comando.prazoId());
        LocalDate antes = prazo.getVencimento();
        ContagemPrazoStrategy contagem = contagemDe(prazo);
        int saldo = prazo.getSituacao() == SituacaoPrazo.SUSPENSO
                ? prazo.saldoNaSuspensao(contagem)
                : 0;
        boolean tinhaAlertas = !prazo.getAlertasEmitidos().isEmpty();
        boolean reiniciou = prazo.retomar(comando.retomada(), relogio.hoje(), contagem) && tinhaAlertas;
        List<PeriodoSuspensao> suspensoes = prazo.getSuspensoes();
        PeriodoSuspensao encerrada = suspensoes.get(suspensoes.size() - 1);
        Prazo salvo = prazos.salvar(prazo);
        registrar(salvo, TipoEventoPrazo.RETOMADA, antes, salvo.getVencimento(),
                TextoDaTrilhaDePrazo.retomada(salvo, encerrada, saldo, reiniciou), comando.autor());
        return salvo;
    }

    @Override
    @Transactional
    public Prazo executar(PrazosUseCases.DilatarPrazo.Comando comando) {
        Prazo prazo = carregar(comando.prazoId());
        LocalDate antes = prazo.getVencimento();
        boolean tinhaAlertas = !prazo.getAlertasEmitidos().isEmpty();
        boolean reiniciou = prazo.dilatar(comando.dias(), comando.fundamento(), relogio.hoje(), contagemDe(prazo))
                && tinhaAlertas;
        Prazo salvo = prazos.salvar(prazo);
        registrar(salvo, TipoEventoPrazo.DILACAO, antes, salvo.getVencimento(),
                TextoDaTrilhaDePrazo.dilacao(salvo, comando.dias(), comando.fundamento(), reiniciou), comando.autor());
        return salvo;
    }

    @Override
    @Transactional
    public Prazo executar(PrazosUseCases.CancelarPrazo.Comando comando) {
        Prazo prazo = carregar(comando.prazoId());
        exigirResponsavelOuChefe(prazo, comando.autor(), "cancelar");
        prazo.cancelar(comando.motivo());
        Prazo salvo = prazos.salvar(prazo);
        registrar(salvo, TipoEventoPrazo.CANCELAMENTO, salvo.getVencimento(), salvo.getVencimento(),
                TextoDaTrilhaDePrazo.cancelamento(comando.motivo()), comando.autor());
        return salvo;
    }

    @Override
    @Transactional
    public Prazo executar(PrazosUseCases.ReabrirPrazo.Comando comando) {
        Prazo prazo = carregar(comando.prazoId());
        exigirResponsavelOuChefe(prazo, comando.autor(), "reabrir");
        prazo.reabrir(comando.motivo());
        Prazo salvo = prazos.salvar(prazo);
        registrar(salvo, TipoEventoPrazo.REABERTURA, salvo.getVencimento(), salvo.getVencimento(),
                TextoDaTrilhaDePrazo.reabertura(salvo, comando.motivo()), comando.autor());
        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public DetalhePrazo executar(Long prazoId) {
        Prazo prazo = carregar(prazoId);
        LocalDate hoje = relogio.hoje();
        return new DetalhePrazo(
                prazo.getId(),
                prazo.getNumeroProcesso().valor(),
                prazo.getDescricao(),
                prazo.getIntimacao(),
                prazo.getQuantidadeDias(),
                prazo.getRegime(),
                prazo.getPrerrogativa(),
                prazo.getDiasAcrescidos(),
                prazo.diasEfetivos(),
                prazo.getVencimentoOriginal(),
                prazo.getVencimento(),
                prazo.diasRestantes(hoje, contagemDe(prazo)),
                prazo.estaEmAberto() && prazo.venceu(hoje),
                prazo.isFatal(),
                prazo.getSituacao(),
                prazo.getCumpridoEm(),
                prazo.getResponsavel().nome(),
                prazo.getResponsavel().oab(),
                prazo.getAlertasEmitidos(),
                prazo.getSuspensoes(),
                historico.doPrazo(prazo.getId()));
    }

    // --- Apoio ---

    private Prazo carregar(Long prazoId) {
        if (prazoId == null) {
            throw new IllegalArgumentException("informe o prazo");
        }
        return prazos.porId(prazoId)
                .orElseThrow(() -> new NoSuchElementException("prazo não encontrado: " + prazoId));
    }

    private ContagemPrazoStrategy contagemDe(Prazo prazo) {
        return motor.estrategiaPara(prazo.getRegime());
    }

    /**
     * Encerrar ou reabrir muda o que a agenda do escritorio mostra: fica com quem
     * responde pelo prazo ou com o chefe. Suspender, retomar e dilatar registram
     * fato processual, que qualquer advogado do escritorio pode lancar.
     */
    private static void exigirResponsavelOuChefe(Prazo prazo, PrazosUseCases.Autor autor, String operacao) {
        if (autor == null) {
            throw new IllegalArgumentException("informe quem vai " + operacao + " o prazo");
        }
        boolean responsavel = autor.oab() != null
                && autor.oab().equalsIgnoreCase(prazo.getResponsavel().oab());
        if (!responsavel && !autor.chefe()) {
            throw new IllegalStateException("só o responsável pelo prazo ("
                    + prazo.getResponsavel().nome() + ") ou o chefe do escritório podem " + operacao + " o prazo");
        }
    }

    private void registrar(Prazo prazo, TipoEventoPrazo tipo, LocalDate antes, LocalDate depois,
                           String detalhe, PrazosUseCases.Autor autor) {
        PrazosUseCases.Autor quem = PrazosUseCases.Autor.ouSistema(autor);
        historico.registrar(EventoPrazo.novo(prazo.getId(), tipo, relogio.agora(), antes, depois,
                detalhe, quem.oab(), quem.nome()));
    }
}
