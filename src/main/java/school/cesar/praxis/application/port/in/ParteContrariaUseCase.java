package school.cesar.praxis.application.port.in;

import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.domain.partecontraria.TipoPessoa;

import java.util.List;

/**
 * Port (Interface de Entrada) que define os casos de uso de Partes Contrárias.
 */
public interface ParteContrariaUseCase {

    /**
     * Criar uma nova parte contrária
     */
    ParteContrariaResponse criarParteContraria(CriarParteContrariaRequest request);

    /**
     * Consultar parte contrária por ID
     */
    ParteContrariaResponse consultarParteContraria(Long id);

    /**
     * Consultar parte contrária por CPF/CNPJ
     */
    ParteContrariaResponse consultarPorCpfOuCnpj(String cpfOuCnpj);

    /**
     * Listar todas as partes contrárias ativas
     */
    List<ParteContrariaResponse> listarPartesContrarias();

    /**
     * Listar partes contrárias por tipo (Física ou Jurídica)
     */
    List<ParteContrariaResponse> listarPorTipo(TipoPessoa tipo);

    /**
     * Listar partes contrárias por cidade
     */
    List<ParteContrariaResponse> listarPorCidade(String cidade);

    /**
     * Editar uma parte contrária existente
     */
    ParteContrariaResponse editarParteContraria(Long id, CriarParteContrariaRequest request);

    /**
     * Deletar (desativar) uma parte contrária
     */
    void deletarParteContraria(Long id);

    /**
     * Reativar uma parte contrária desativada
     */
    void reativarParteContraria(Long id);
}
