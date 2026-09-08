package school.cesar.praxis.domain.processo;

import jakarta.persistence.*;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.domain.prazo.Prazo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Raiz do agregado do contexto Gestao de Processos.
 *
 * <p>Padroes: Observer (advogados responsaveis assinam eventos) e Iterator
 * (linha do tempo de andamentos em ordem cronologica, sem expor a colecao).
 */
@Entity
@Table(name = "processo")
public class Processo implements Iterable<Andamento> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "numero_cnj", nullable = false, unique = true))
    private NumeroCnj numero;

    @Column(nullable = false, length = 200)
    private String cliente;

    @Column(nullable = false)
    private boolean segredoJustica;

    @Column(length = 120)
    private String responsavelNome;

    @Column(length = 160)
    private String responsavelEmail;

    @Column(length = 20)
    private String responsavelOab;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "processo_id")
    private final List<Andamento> andamentos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "processo_id")
    private final List<Prazo> prazos = new ArrayList<>();

    @Transient
    private final List<ObservadorProcesso> observadores = new ArrayList<>();

    protected Processo() {
    }

    public Processo(NumeroCnj numero, String cliente, boolean segredoJustica) {
        this.numero = numero;
        this.cliente = cliente;
        this.segredoJustica = segredoJustica;
    }

    /** Define o advogado responsavel pelos autos (destinatario das notificacoes). */
    public void definirResponsavel(String nome, String email, String oab) {
        this.responsavelNome = nome;
        this.responsavelEmail = email;
        this.responsavelOab = oab;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public String getResponsavelEmail() {
        return responsavelEmail;
    }

    public String getResponsavelOab() {
        return responsavelOab;
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

    // --- Regras de negocio ---

    public void registrarAndamento(Andamento andamento) {
        andamentos.add(andamento);
        publicar(new EventoProcesso.AndamentoRegistrado(numero.valor(), andamento.getDescricao()));
    }

    public void adicionarPrazo(Prazo prazo) {
        prazos.add(prazo);
    }

    /**
     * Varre os prazos e publica evento para cada prazo fatal em risco.
     *
     * @return quantidade de prazos que dispararam alerta
     */
    public int verificarPrazos(LocalDate hoje, ContagemPrazoStrategy contagem) {
        int alertados = 0;
        for (Prazo prazo : prazos) {
            if (prazo.emRisco(hoje, contagem)) {
                publicar(new EventoProcesso.PrazoEmRisco(
                        numero.valor(),
                        prazo.getDescricao(),
                        prazo.diasRestantes(hoje, contagem)));
                alertados++;
            }
        }
        return alertados;
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

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public List<Prazo> getPrazos() {
        return List.copyOf(prazos);
    }

    public int quantidadeAndamentos() {
        return andamentos.size();
    }
}
