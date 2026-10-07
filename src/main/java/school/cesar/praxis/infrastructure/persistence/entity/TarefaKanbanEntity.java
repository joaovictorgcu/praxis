package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import school.cesar.praxis.domain.kanban.PrioridadeTarefa;

@Entity
@Table(name = "kanban_tarefa")
public class TarefaKanbanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long versao;

    @Column(name = "numero_processo", nullable = false, length = 30)
    private String numeroProcesso;

    @Column(name = "coluna_id", nullable = false)
    private Long colunaId;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "descricao", length = 3000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 20)
    private PrioridadeTarefa prioridade;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "criada_em", nullable = false)
    private LocalDate criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDate atualizadaEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "kanban_checklist", joinColumns = @JoinColumn(name = "tarefa_id"))
    @OrderColumn(name = "ordem")
    private List<ItemChecklistEntity> checklist = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersao() {
        return versao;
    }

    public void setVersao(long versao) {
        this.versao = versao;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public Long getColunaId() {
        return colunaId;
    }

    public void setColunaId(Long colunaId) {
        this.colunaId = colunaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public LocalDate getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDate criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDate getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDate atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    public List<ItemChecklistEntity> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<ItemChecklistEntity> checklist) {
        this.checklist = checklist;
    }
}
