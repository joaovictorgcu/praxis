package school.cesar.praxis.domain.processo;

import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Raiz de agregado do subdominio nuclear <b>Gestao de Processos</b>.
 *
 * <p>Padroes aplicados aqui:
 * <ul>
 *   <li><b>Observer</b> - observadores assinam o agregado e recebem eventos de dominio;</li>
 *   <li><b>Iterator</b> - a linha do tempo de andamentos e percorrida em ordem
 *       cronologica sem expor a colecao interna.</li>
 * </ul>
 */
public class Processo implements Iterable<Andamento> {

    private final Long id;
    private final NumeroCnj numero;
    private final String cliente;
    private final String comarca;
    private final boolean segredoJustica;
    private final Advogado responsavel;
    private final List<Andamento> andamentos = new ArrayList<>();
    private final List<ObservadorProcesso> observadores = new ArrayList<>();

    public Processo(Long id,
                    NumeroCnj numero,
                    String cliente,
                    String comarca,
                    boolean segredoJustica,
                    Advogado responsavel) {
        if (numero == null) {
            throw new IllegalArgumentException("numero CNJ e obrigatorio");
        }
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("cliente e obrigatorio");
        }
        if (comarca == null || comarca.isBlank()) {
            throw new IllegalArgumentException("comarca e obrigatoria");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("advogado responsavel e obrigatorio");
        }
        this.id = id;
        this.numero = numero;
        this.cliente = cliente;
        this.comarca = comarca;
        this.segredoJustica = segredoJustica;
        this.responsavel = responsavel;
    }

    public Processo(NumeroCnj numero,
                    String cliente,
                    String comarca,
                    boolean segredoJustica,
                    Advogado responsavel) {
        this(null, numero, cliente, comarca, segredoJustica, responsavel);
    }

    // --- Observer ---

    public void assinar(ObservadorProcesso observador) {
        observadores.add(observador);
    }

    private void publicar(EventoProcesso evento) {
        for (ObservadorProcesso observador : observadores) {
            observador.notificar(evento);
        }
    }

    // --- Comportamento de dominio ---

    /** Registra andamento e avisa os observadores (Observer). */
    public void registrarAndamento(Andamento andamento) {
        if (andamento == null) {
            throw new IllegalArgumentException("andamento e obrigatorio");
        }
        andamentos.add(andamento);
        publicar(new EventoProcesso.AndamentoRegistrado(
                numero.valor(), responsavel, andamento.getDescricao(), andamento.getData()));
    }

    public void restaurarAndamento(Andamento andamento) {
        andamentos.add(andamento);
    }

    /** Ultimo andamento que inicia contagem de prazo, se houver. */
    public Andamento ultimaIntimacao() {
        Andamento encontrado = null;
        for (Andamento andamento : this) {
            if (andamento.iniciaContagemDePrazo()) {
                encontrado = andamento;
            }
        }
        return encontrado;
    }

    // --- Iterator: linha do tempo cronologica ---

    @Override
    public Iterator<Andamento> iterator() {
        List<Andamento> ordenados = new ArrayList<>(andamentos);
        ordenados.sort(Comparator.comparing(Andamento::getData));
        return List.copyOf(ordenados).iterator();
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumero() {
        return numero;
    }

    public String getCliente() {
        return cliente;
    }

    public String getComarca() {
        return comarca;
    }

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public Advogado getResponsavel() {
        return responsavel;
    }

    public int quantidadeAndamentos() {
        return andamentos.size();
    }
}
