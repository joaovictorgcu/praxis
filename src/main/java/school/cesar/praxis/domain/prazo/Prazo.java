package school.cesar.praxis.domain.prazo;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Entidade: prazo processual vinculado a um processo. Fatal = perda do direito. */
@Entity
@Table(name = "prazo")
public class Prazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private LocalDate intimacao;

    @Column(nullable = false)
    private int quantidadeDias;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(nullable = false)
    private boolean fatal;

    @Column(nullable = false, length = 30)
    private String regimeContagem;

    @Column(nullable = false)
    private boolean cumprido;

    protected Prazo() {
    }

    public Prazo(String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 boolean fatal,
                 ContagemPrazoStrategy contagem) {
        if (quantidadeDias <= 0) {
            throw new IllegalArgumentException("prazo deve ter ao menos 1 dia");
        }
        this.descricao = descricao;
        this.intimacao = intimacao;
        this.quantidadeDias = quantidadeDias;
        this.fatal = fatal;
        this.regimeContagem = contagem.nome();
        this.vencimento = contagem.calcularVencimento(intimacao, quantidadeDias);
        this.cumprido = false;
    }

    public int diasRestantes(LocalDate hoje, ContagemPrazoStrategy contagem) {
        return contagem.diasRestantes(hoje, vencimento);
    }

    /** Regra de negocio: prazo fatal em risco quando faltam 3 dias contaveis ou menos. */
    public boolean emRisco(LocalDate hoje, ContagemPrazoStrategy contagem) {
        return fatal && !cumprido && diasRestantes(hoje, contagem) <= 3;
    }

    public void marcarCumprido() {
        if (cumprido) {
            throw new IllegalStateException("prazo ja cumprido");
        }
        this.cumprido = true;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getIntimacao() {
        return intimacao;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public boolean isFatal() {
        return fatal;
    }

    public boolean isCumprido() {
        return cumprido;
    }

    public String getRegimeContagem() {
        return regimeContagem;
    }

    public int getQuantidadeDias() {
        return quantidadeDias;
    }
}
