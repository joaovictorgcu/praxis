package school.cesar.praxis.application.usecase;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.equipe.Equipe;

@Service
public class EquipeAppService
    implements
        EquipesUseCases.CadastrarEquipe,
        EquipesUseCases.ListarEquipes,
        EquipesUseCases.AtualizarEquipe,
        EquipesUseCases.ExcluirEquipe
{

    private final EquipeRepositorio equipes;

    private final AdvogadoRepositorio advogados;

    public EquipeAppService(EquipeRepositorio equipes, AdvogadoRepositorio advogados) {
        this.advogados = advogados;
        this.equipes = equipes;
    }

    @Override
    @Transactional
    public EquipesUseCases.ItemEquipe executar(EquipesUseCases.CadastrarEquipe.Comando comando) {
        validarMembros(comando.membrosIds());
        Equipe salva = equipes.salvar(new Equipe(null, comando.nome(), comando.membrosIds()));
        return paraItem(salva);
    }

    @Override
    public List<EquipesUseCases.ItemEquipe> executar() {
        return equipes.listarTodas().stream().map(EquipeAppService::paraItem).toList();
    }

    @Override
    @Transactional
    public EquipesUseCases.ItemEquipe executar(EquipesUseCases.AtualizarEquipe.Comando comando) {
        Equipe equipe = carregar(equipes, comando.id());
        validarMembros(comando.membrosIds());
        equipe.renomear(comando.nome());
        equipe.getMembrosIds().forEach(equipe::removerMembro);
        if (comando.membrosIds() != null) {
            comando.membrosIds().forEach(equipe::adicionarMembro);
        }
        return paraItem(equipes.salvar(equipe));
    }

    @Override
    @Transactional
    public void executar(Long id) {
        carregar(equipes, id);
        equipes.excluir(id);
    }

    private void validarMembros(Set<Long> ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            if (id == null || advogados.porId(id).isEmpty()) {
                throw new IllegalArgumentException("Advogado não encontrado: " + id);
            }
        }
    }

    static Equipe carregar(EquipeRepositorio equipes, Long id) {
        return equipes
            .porId(id)
            .orElseThrow(() -> new NoSuchElementException("equipe nao encontrada: " + id));
    }

    static EquipesUseCases.ItemEquipe paraItem(Equipe equipe) {
        return new EquipesUseCases.ItemEquipe(equipe.getId(), equipe.getNome(), equipe.getMembrosIds());
    }
}
