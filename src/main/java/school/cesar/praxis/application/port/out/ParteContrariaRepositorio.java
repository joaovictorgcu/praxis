package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.domain.partecontraria.ParteContraria;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do agregado ParteContraria. */
public interface ParteContrariaRepositorio {

    ParteContraria salvar(ParteContraria parteContraria);

    Optional<ParteContraria> porId(Long id);

    /** Nao ha unicidade no banco para parte contraria: so a ativa importa na checagem. */
    Optional<ParteContraria> porCpfOuCnpjAtiva(String cpfOuCnpj);

    List<ParteContraria> listarAtivas();

    List<ParteContraria> listarTodas();

    List<ParteContraria> porTipoAtivas(TipoPessoa tipo);

    List<ParteContraria> porCidadeAtivas(String cidade);
}