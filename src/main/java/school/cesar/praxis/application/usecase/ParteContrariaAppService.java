package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.domain.partecontraria.ParteContraria;
import school.cesar.praxis.domain.partecontraria.TipoPessoa;
import school.cesar.praxis.infrastructure.persistence.ParteContrariaRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de Aplicação para Partes Contrárias.
 * Implementa os casos de uso definidos no port.
 */
@Service
@Transactional
public class ParteContrariaAppService implements ParteContrariaUseCase {

    private final ParteContrariaRepository parteContrariaRepository;

    public ParteContrariaAppService(ParteContrariaRepository parteContrariaRepository) {
        this.parteContrariaRepository = parteContrariaRepository;
    }

    @Override
    public ParteContrariaResponse criarParteContraria(CriarParteContrariaRequest request) {
        // Validar se já não existe outra parte com o mesmo CPF/CNPJ
        if (parteContrariaRepository.findByCpfOuCnpjAndAtivaTrue(request.getCpfOuCnpj()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma parte contrária ativa com este CPF/CNPJ");
        }

        ParteContraria novaParteContraria = new ParteContraria(
            request.getNome(),
            request.getCpfOuCnpj(),
            request.getTipoPessoa()
        );

        // Adicionar dados complementares se fornecidos
        if (request.getEmail() != null) {
            novaParteContraria.setEmail(request.getEmail());
        }
        if (request.getTelefone() != null) {
            novaParteContraria.setTelefone(request.getTelefone());
        }
        if (request.getEndereco() != null) {
            novaParteContraria.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            novaParteContraria.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            novaParteContraria.setEstado(request.getEstado());
        }
        if (request.getCep() != null) {
            novaParteContraria.setCep(request.getCep());
        }
        if (request.getObservacoes() != null) {
            novaParteContraria.setObservacoes(request.getObservacoes());
        }

        ParteContraria parteSalva = parteContrariaRepository.save(novaParteContraria);
        return converterParaResponse(parteSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public ParteContrariaResponse consultarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepository.findById(id)
            .filter(ParteContraria::isAtiva)
            .orElse(null);
        
        return parteContraria != null ? converterParaResponse(parteContraria) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public ParteContrariaResponse consultarPorCpfOuCnpj(String cpfOuCnpj) {
        ParteContraria parteContraria = parteContrariaRepository.findByCpfOuCnpjAndAtivaTrue(cpfOuCnpj)
            .orElse(null);
        
        return parteContraria != null ? converterParaResponse(parteContraria) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPartesContrarias() {
        return parteContrariaRepository.findByAtivaTrue()
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPorTipo(TipoPessoa tipo) {
        return parteContrariaRepository.findByTipoPessoaAndAtivaTrue(tipo)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPorCidade(String cidade) {
        return parteContrariaRepository.findByCidadeAndAtivaTrue(cidade)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ParteContrariaResponse editarParteContraria(Long id, CriarParteContrariaRequest request) {
        ParteContraria parteContraria = parteContrariaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Parte contrária não encontrada com ID: " + id));

        parteContraria.atualizar(
            request.getNome(),
            request.getEmail(),
            request.getTelefone(),
            request.getEndereco(),
            request.getCidade(),
            request.getEstado(),
            request.getCep(),
            request.getObservacoes()
        );

        ParteContraria parteAtualizada = parteContrariaRepository.save(parteContraria);
        return converterParaResponse(parteAtualizada);
    }

    @Override
    public void deletarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Parte contrária não encontrada com ID: " + id));
        
        parteContraria.desativar();
        parteContrariaRepository.save(parteContraria);
    }

    @Override
    public void reativarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Parte contrária não encontrada com ID: " + id));
        
        if (parteContraria.isAtiva()) {
            throw new IllegalArgumentException("Parte contrária já está ativa");
        }
        
        parteContraria.reativar();
        parteContrariaRepository.save(parteContraria);
    }

    // Método auxiliar

    private ParteContrariaResponse converterParaResponse(ParteContraria parteContraria) {
        return new ParteContrariaResponse(
            parteContraria.getId(),
            parteContraria.getNome(),
            parteContraria.getCpfOuCnpj(),
            parteContraria.getTipoPessoa(),
            parteContraria.getEmail(),
            parteContraria.getTelefone(),
            parteContraria.getEndereco(),
            parteContraria.getCidade(),
            parteContraria.getEstado(),
            parteContraria.getCep(),
            parteContraria.getObservacoes(),
            parteContraria.isAtiva(),
            parteContraria.getCriadaEm(),
            parteContraria.getAtualizadaEm()
        );
    }
}
