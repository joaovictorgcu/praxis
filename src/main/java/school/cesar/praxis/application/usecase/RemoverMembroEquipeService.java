package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.equipe.Equipe;

@Service
public class RemoverMembroEquipeService implements EquipesUseCases.RemoverMembro {

    private final EquipeRepositorio equipes;

    public RemoverMembroEquipeService(EquipeRepositorio equipes) {
        this.equipes = equipes;
    }

    @Override
    @Transactional
    public EquipesUseCases.ItemEquipe executar(Long equipeId, Long advogadoId) {
        Equipe equipe = EquipeAppService.carregar(equipes, equipeId);
        equipe.removerMembro(advogadoId);
        return EquipeAppService.paraItem(equipes.salvar(equipe));
    }
}
