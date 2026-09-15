package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.equipe.Equipe;

@Service
public class AdicionarMembroEquipeService implements EquipesUseCases.AdicionarMembro {

    private final EquipeRepositorio equipes;

    public AdicionarMembroEquipeService(EquipeRepositorio equipes) {
        this.equipes = equipes;
    }

    @Override
    @Transactional
    public EquipesUseCases.ItemEquipe executar(Long equipeId, Long advogadoId) {
        Equipe equipe = EquipeAppService.carregar(equipes, equipeId);
        equipe.adicionarMembro(advogadoId);
        return EquipeAppService.paraItem(equipes.salvar(equipe));
    }
}
