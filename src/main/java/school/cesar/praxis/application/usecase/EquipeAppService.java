package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.equipe.Equipe;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EquipeAppService implements EquipesUseCases.CadastrarEquipe,
        EquipesUseCases.ListarEquipes {

    private final EquipeRepositorio equipes;

    public EquipeAppService(EquipeRepositorio equipes) {
        this.equipes = equipes;
    }

    @Override
    @Transactional
    public EquipesUseCases.ItemEquipe executar(EquipesUseCases.CadastrarEquipe.Comando comando) {
        Equipe salva = equipes.salvar(new Equipe(comando.nome()));
        return paraItem(salva);
    }

    @Override
    public List<EquipesUseCases.ItemEquipe> executar() {
        return equipes.listarTodas().stream()
                .map(EquipeAppService::paraItem)
                .toList();
    }

    static Equipe carregar(EquipeRepositorio equipes, Long id) {
        return equipes.porId(id)
                .orElseThrow(() -> new NoSuchElementException("equipe nao encontrada: " + id));
    }

    static EquipesUseCases.ItemEquipe paraItem(Equipe equipe) {
        return new EquipesUseCases.ItemEquipe(equipe.getId(), equipe.getNome(), equipe.getMembrosIds());
    }
}
