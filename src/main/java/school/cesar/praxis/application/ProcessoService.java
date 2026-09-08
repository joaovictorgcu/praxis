package school.cesar.praxis.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.domain.notificacao.AdvogadoResponsavel;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.processo.Andamento;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.processo.TipoAndamento;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.infrastructure.persistence.ProcessoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** Camada de aplicacao: orquestra casos de uso, sem regra de negocio propria. */
@Service
public class ProcessoService {

    private final ProcessoRepository repositorio;
    private final Notificador notificador;
    private final ContagemPrazoStrategy contagem;

    public ProcessoService(ProcessoRepository repositorio,
                           Notificador notificador,
                           ContagemPrazoStrategy contagem) {
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.contagem = contagem;
    }

    @Transactional
    public Processo cadastrar(String numeroCnj,
                             String cliente,
                             boolean segredoJustica,
                             String responsavelNome,
                             String responsavelEmail,
                             String responsavelOab) {
        Processo processo = new Processo(new NumeroCnj(numeroCnj), cliente, segredoJustica);
        processo.definirResponsavel(responsavelNome, responsavelEmail, responsavelOab);
        return repositorio.save(processo);
    }

    @Transactional
    public Processo registrarAndamento(String numeroCnj,
                                      LocalDate data,
                                      String descricao,
                                      TipoAndamento tipo) {
        Processo processo = carregarComObservador(numeroCnj);
        processo.registrarAndamento(new Andamento(data, descricao, tipo));
        return repositorio.save(processo);
    }

    @Transactional
    public Prazo abrirPrazo(String numeroCnj,
                            String descricao,
                            LocalDate intimacao,
                            int dias,
                            boolean fatal) {
        Processo processo = carregarComObservador(numeroCnj);
        Prazo prazo = new Prazo(descricao, intimacao, dias, fatal, contagem);
        processo.adicionarPrazo(prazo);
        repositorio.save(processo);
        return prazo;
    }

    /** Job diario: notifica responsaveis por prazos fatais em risco. */
    @Transactional(readOnly = true)
    public int varrerPrazos(LocalDate hoje) {
        int total = 0;
        for (Processo processo : repositorio.findAll()) {
            vincularObservador(processo);
            total += processo.verificarPrazos(hoje, contagem);
        }
        return total;
    }

    @Transactional(readOnly = true)
    public List<Andamento> linhaDoTempo(String numeroCnj) {
        Processo processo = buscar(numeroCnj);
        List<Andamento> timeline = new java.util.ArrayList<>();
        for (Andamento andamento : processo) {
            timeline.add(andamento);
        }
        return timeline;
    }

    @Transactional(readOnly = true)
    public Processo buscar(String numeroCnj) {
        return repositorio.findByNumeroValor(numeroCnj)
                .orElseThrow(() -> new NoSuchElementException("processo nao encontrado: " + numeroCnj));
    }

    private Processo carregarComObservador(String numeroCnj) {
        Processo processo = buscar(numeroCnj);
        vincularObservador(processo);
        return processo;
    }

    private void vincularObservador(Processo processo) {
        if (processo.getResponsavelEmail() != null) {
            processo.assinar(new AdvogadoResponsavel(
                    processo.getResponsavelNome(), processo.getResponsavelEmail(), notificador));
        }
    }
}
