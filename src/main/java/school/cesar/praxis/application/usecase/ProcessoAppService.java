package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.out.AcessoAoProcessoRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.compartilhado.EventoDeAuditoria;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.Andamento;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.processo.Vara;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do subdominio nuclear. O observador (Observer) e reanexado ao
 * agregado a cada carregamento, porque a assinatura vive em memoria e o
 * agregado vem do banco.
 */
@Service
public class ProcessoAppService implements ProcessosUseCases.CadastrarProcesso,
        ProcessosUseCases.RegistrarAndamento,
        ProcessosUseCases.ConsultarLinhaDoTempo,
        ProcessosUseCases.ListarProcessos,
        ProcessosUseCases.ConsultarProcesso {

    private final ProcessoRepositorio processos;
    private final ObservadorProcesso observador;
    private final AcessoAoProcessoRepositorio acessos;
    private final RegistradorDeAuditoria auditoria;
    private final Relogio relogio;

    public ProcessoAppService(ProcessoRepositorio processos, ObservadorProcesso observador,
                              AcessoAoProcessoRepositorio acessos, RegistradorDeAuditoria auditoria,
                              Relogio relogio) {
        this.processos = processos;
        this.observador = observador;
        this.acessos = acessos;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Processo executar(ProcessosUseCases.CadastrarProcesso.Comando comando) {
        Vara vara = comando.varaTribunal() == null && comando.varaNumero() == null
                ? null
                : new Vara(comando.varaTribunal(), comando.comarca(), comando.varaNumero());

        Processo processo = new Processo(
                null,
                NumeroCnj.de(comando.numeroCnj()),
                comando.cliente(),
                comando.comarca(),
                comando.segredoJustica(),
                new Advogado(comando.responsavelNome(), comando.responsavelEmail(), comando.responsavelOab()),
                vara);
        Processo salvo = processos.salvar(processo);

        // Sigiloso: o responsavel recebe acesso automatico e permanente aos autos
        // inteiros, para nao ficar bloqueado dos proprios autos no mesmo instante
        // em que os cadastra.
        if (salvo.isSegredoJustica()) {
            String oabResponsavel = salvo.getResponsavel().oab();
            acessos.salvar(new AcessoAoProcesso(salvo.getNumero(), oabResponsavel, oabResponsavel,
                    relogio.hoje(), null));
            auditoria.registrar(EventoDeAuditoria.gestao("CONCESSAO", salvo.getNumero(), oabResponsavel,
                    null, oabResponsavel, LocalDateTime.now(),
                    "acesso automático e permanente do responsável ao cadastrar processo sigiloso"));
        }

        return salvo;
    }

    @Override
    @Transactional
    public Processo executar(ProcessosUseCases.RegistrarAndamento.Comando comando) {
        Processo processo = carregar(comando.numeroCnj());
        processo.assinar(observador);
        processo.registrarAndamento(new Andamento(comando.data(), comando.descricao(), comando.tipo()));
        return processos.salvar(processo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Andamento> executar(String numeroCnj) {
        Processo processo = carregar(numeroCnj);
        List<Andamento> timeline = new ArrayList<>();
        for (Andamento andamento : processo) {
            timeline.add(andamento);
        }
        return timeline;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Processo> executar() {
        return processos.listar();
    }

    /** Publico para a tela do processo; e o mesmo carregamento dos demais casos de uso. */
    @Override
    @Transactional(readOnly = true)
    public Processo consultar(String numeroCnj) {
        return carregar(numeroCnj);
    }

    private Processo carregar(String numeroCnj) {
        return processos.porNumero(NumeroCnj.de(numeroCnj))
                .orElseThrow(() -> new NoSuchElementException("processo não encontrado: " + numeroCnj));
    }
}
