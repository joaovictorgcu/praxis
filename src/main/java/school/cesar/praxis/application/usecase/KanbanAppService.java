package school.cesar.praxis.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.KanbanUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.application.port.out.KanbanRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.kanban.ColunaKanban;
import school.cesar.praxis.domain.kanban.Tarefa;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

@Service
public class KanbanAppService
    implements
        KanbanUseCases.ConsultarQuadro,
        KanbanUseCases.CriarTarefa,
        KanbanUseCases.AtualizarTarefa,
        KanbanUseCases.MoverTarefa,
        KanbanUseCases.ExcluirTarefa,
        KanbanUseCases.AtualizarChecklist,
        KanbanUseCases.ConfigurarColunas
{

    private final KanbanRepositorio repositorio;
    private final ProcessoRepositorio processos;
    private final AdvogadoRepositorio advogados;
    private final Relogio relogio;

    public KanbanAppService(
        KanbanRepositorio repositorio,
        ProcessoRepositorio processos,
        AdvogadoRepositorio advogados,
        Relogio relogio
    ) {
        this.repositorio = repositorio;
        this.processos = processos;
        this.advogados = advogados;
        this.relogio = relogio;
    }

    @Override
    @Transactional(readOnly = true)
    public KanbanUseCases.Quadro executar(KanbanUseCases.Acesso acesso) {
        List<Processo> visiveis = processos
            .listar()
            .stream()
            .filter(p -> podeAcessar(p, acesso))
            .toList();
        Set<String> numeros = visiveis
            .stream()
            .map(p -> p.getNumero().valor())
            .collect(Collectors.toSet());
        List<ColunaKanban> colunas = repositorio.listarColunas();
        return new KanbanUseCases.Quadro(
            colunas,
            repositorio
                .listarTarefas()
                .stream()
                .filter(t -> numeros.contains(t.numeroProcesso()))
                .toList(),
            visiveis
                .stream()
                .map(p ->
                    new KanbanUseCases.ProcessoResumo(
                        p.getNumero().valor(),
                        p.getCliente(),
                        p.getResponsavel().nome(),
                        p.isSegredoJustica()
                    )
                )
                .toList(),
            revisao(colunas),
            relogio.hoje()
        );
    }

    @Override
    @Transactional
    public Tarefa executar(KanbanUseCases.CriarTarefa.Comando comando, KanbanUseCases.Acesso acesso) {
        repositorio.bloquearColunas();
        validarProcesso(comando.numeroProcesso(), acesso);
        validarColuna(comando.colunaId());
        validarResponsavel(comando.responsavelId());
        return repositorio.salvarTarefa(
            new Tarefa(
                null,
                comando.numeroProcesso(),
                comando.colunaId(),
                comando.titulo(),
                comando.descricao(),
                comando.prioridade(),
                comando.responsavelId(),
                comando.vencimento(),
                comando.checklist(),
                tarefasDa(comando.colunaId()).size(),
                relogio.hoje(),
                relogio.hoje(),
                0
            )
        );
    }

    @Override
    @Transactional
    public Tarefa executar(KanbanUseCases.AtualizarTarefa.Comando comando, KanbanUseCases.Acesso acesso) {
        repositorio.bloquearColunas();
        Tarefa anterior = carregar(comando.id(), comando.versao(), acesso);
        validarProcesso(comando.numeroProcesso(), acesso);
        validarColuna(comando.colunaId());
        validarResponsavel(comando.responsavelId());
        boolean mudou = !anterior.colunaId().equals(comando.colunaId());
        Tarefa salva = repositorio.salvarTarefa(
            new Tarefa(
                anterior.id(),
                comando.numeroProcesso(),
                comando.colunaId(),
                comando.titulo(),
                comando.descricao(),
                comando.prioridade(),
                comando.responsavelId(),
                comando.vencimento(),
                comando.checklist(),
                mudou ? tarefasDa(comando.colunaId()).size() : anterior.ordem(),
                anterior.criadaEm(),
                relogio.hoje(),
                anterior.versao()
            )
        );
        if (mudou) {
            ordenar(tarefasDa(anterior.colunaId()), anterior.colunaId());
        }
        return salva;
    }

    @Override
    @Transactional
    public Tarefa executar(KanbanUseCases.MoverTarefa.Comando comando, KanbanUseCases.Acesso acesso) {
        repositorio.bloquearColunas();
        Tarefa tarefa = carregar(comando.id(), comando.versao(), acesso);
        validarColuna(comando.colunaId());
        if (comando.id().equals(comando.antesDeId())) {
            return tarefa;
        }
        List<Tarefa> destino = new ArrayList<>(tarefasDa(comando.colunaId()));
        destino.removeIf(t -> t.id().equals(tarefa.id()));
        int posicao = destino.size();
        if (comando.antesDeId() != null) {
            Tarefa referencia = destino
                .stream()
                .filter(t -> t.id().equals(comando.antesDeId()))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("O cartão de referência não está nesta coluna.")
                );
            validarProcesso(referencia.numeroProcesso(), acesso);
            posicao = destino.indexOf(referencia);
        }
        destino.add(posicao, tarefa);
        ordenar(destino, comando.colunaId());
        if (!tarefa.colunaId().equals(comando.colunaId())) {
            ordenar(tarefasDa(tarefa.colunaId()), tarefa.colunaId());
        }
        return repositorio.tarefaPorId(tarefa.id()).orElseThrow();
    }

    @Override
    @Transactional
    public void executar(Long id, long versao, KanbanUseCases.Acesso acesso) {
        repositorio.bloquearColunas();
        Tarefa tarefa = carregar(id, versao, acesso);
        repositorio.excluirTarefa(id);
        ordenar(tarefasDa(tarefa.colunaId()), tarefa.colunaId());
    }

    @Override
    @Transactional
    public Tarefa executar(KanbanUseCases.AtualizarChecklist.Comando comando, KanbanUseCases.Acesso acesso) {
        repositorio.bloquearColunas();
        Tarefa tarefa = carregar(comando.id(), comando.versao(), acesso);
        return repositorio.salvarTarefa(tarefa.atualizarChecklist(comando.checklist(), relogio.hoje()));
    }

    @Override
    @Transactional
    public KanbanUseCases.Quadro executar(
        KanbanUseCases.ConfigurarColunas.Comando comando,
        KanbanUseCases.Acesso acesso
    ) {
        if (!acesso.chefe()) {
            throw new ProxyDeAcesso.AcessoNegadoException("Somente o chefe pode configurar as colunas.");
        }
        List<ColunaKanban> atuais = repositorio.bloquearColunas();
        if (!revisao(atuais).equals(comando.revisao())) {
            throw new IllegalStateException(
                "As colunas foram alteradas em outra sessão. Recarregue o quadro antes de configurar."
            );
        }
        if (comando.colunas() == null || comando.colunas().isEmpty() || comando.colunas().size() > 12) {
            throw new IllegalArgumentException("O quadro deve ter entre 1 e 12 colunas.");
        }
        Set<Long> mantidas = new HashSet<>();
        Set<String> nomes = new HashSet<>();
        List<ColunaKanban> configuradas = new ArrayList<>();
        for (int i = 0; i < comando.colunas().size(); i++) {
            KanbanUseCases.ConfigurarColunas.Coluna c = comando.colunas().get(i);
            if (c == null) {
                throw new IllegalArgumentException("Coluna inválida.");
            }
            ColunaKanban anterior =
                c.id() == null
                    ? null
                    : atuais
                          .stream()
                          .filter(a -> a.id().equals(c.id()))
                          .findFirst()
                          .orElseThrow(() -> new NoSuchElementException("Coluna não encontrada."));
            if (c.id() != null && !mantidas.add(c.id())) {
                throw new IllegalArgumentException("Coluna repetida.");
            }
            ColunaKanban nova = new ColunaKanban(
                c.id(),
                c.nome(),
                c.cor(),
                i,
                c.conclusiva(),
                anterior == null ? 0 : anterior.versao()
            );
            if (!nomes.add(nova.nome().toLowerCase(java.util.Locale.ROOT))) {
                throw new IllegalArgumentException("Use nomes diferentes para as colunas.");
            }
            configuradas.add(nova);
        }
        Map<Long, Long> destinos = comando.destinos() == null ? Map.of() : comando.destinos();
        List<ColunaKanban> removidas = atuais
            .stream()
            .filter(c -> !mantidas.contains(c.id()))
            .toList();
        for (ColunaKanban removida : removidas) {
            List<Tarefa> tarefas = tarefasDa(removida.id());
            if (!tarefas.isEmpty()) {
                Long destino = destinos.get(removida.id());
                if (destino == null || !mantidas.contains(destino)) {
                    throw new IllegalArgumentException(
                        "Escolha uma coluna existente de destino para as tarefas de “" +
                            removida.nome() +
                            "”."
                    );
                }
            }
        }
        configuradas.forEach(repositorio::salvarColuna);
        for (ColunaKanban removida : removidas) {
            List<Tarefa> tarefas = tarefasDa(removida.id());
            if (!tarefas.isEmpty()) {
                Long destino = destinos.get(removida.id());
                List<Tarefa> juntas = new ArrayList<>(tarefasDa(destino));
                juntas.addAll(tarefas);
                ordenar(juntas, destino);
            }
            repositorio.excluirColuna(removida.id());
        }
        return executar(acesso);
    }

    private void ordenar(List<Tarefa> tarefas, Long coluna) {
        for (int i = 0; i < tarefas.size(); i++) {
            Tarefa tarefa = tarefas.get(i);
            if (!tarefa.colunaId().equals(coluna) || tarefa.ordem() != i) {
                repositorio.salvarTarefa(tarefa.mover(coluna, i, relogio.hoje()));
            }
        }
    }

    private List<Tarefa> tarefasDa(Long coluna) {
        return repositorio
            .listarTarefas()
            .stream()
            .filter(t -> t.colunaId().equals(coluna))
            .sorted(Comparator.comparingInt(Tarefa::ordem).thenComparing(Tarefa::id))
            .toList();
    }

    private Tarefa carregar(Long id, long versao, KanbanUseCases.Acesso acesso) {
        Tarefa tarefa = repositorio
            .tarefaPorId(id)
            .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));
        validarProcesso(tarefa.numeroProcesso(), acesso);
        if (tarefa.versao() != versao) {
            throw new IllegalStateException(
                "Esta tarefa foi alterada em outra sessão. Atualize o quadro e tente novamente."
            );
        }
        return tarefa;
    }

    private void validarColuna(Long id) {
        if (
            id == null ||
            repositorio
                .listarColunas()
                .stream()
                .noneMatch(c -> c.id().equals(id))
        ) {
            throw new IllegalArgumentException("Selecione uma coluna existente.");
        }
    }

    private void validarResponsavel(Long id) {
        if (id != null && advogados.porId(id).isEmpty()) {
            throw new IllegalArgumentException("O responsável selecionado não existe.");
        }
    }

    private void validarProcesso(String numero, KanbanUseCases.Acesso acesso) {
        Processo processo = processos
            .porNumero(new NumeroCnj(numero))
            .orElseThrow(() -> new NoSuchElementException("Processo não encontrado."));
        if (!podeAcessar(processo, acesso)) {
            throw new ProxyDeAcesso.AcessoNegadoException(
                "Sua OAB não tem acesso às tarefas deste processo sigiloso."
            );
        }
    }

    private static boolean podeAcessar(Processo processo, KanbanUseCases.Acesso acesso) {
        return (
            acesso.chefe() ||
            !processo.isSegredoJustica() ||
            processo.getResponsavel().oab().equalsIgnoreCase(acesso.oab())
        );
    }

    private static String revisao(List<ColunaKanban> colunas) {
        return colunas
            .stream()
            .sorted(Comparator.comparing(ColunaKanban::id))
            .map(c -> c.id() + ":" + c.versao())
            .collect(Collectors.joining("|"));
    }
}
