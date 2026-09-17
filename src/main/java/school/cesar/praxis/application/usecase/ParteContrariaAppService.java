package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.application.port.out.ParteContrariaRepositorio;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.domain.partecontraria.ParteContraria;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParteContrariaAppService implements ParteContrariaUseCase {

    private final ParteContrariaRepositorio parteContrariaRepositorio;

    public ParteContrariaAppService(ParteContrariaRepositorio parteContrariaRepositorio) {
        this.parteContrariaRepositorio = parteContrariaRepositorio;
    }

    @Override
    public ParteContrariaResponse criarParteContraria(CriarParteContrariaRequest request) {
        if (parteContrariaRepositorio.porCpfOuCnpjAtiva(request.getCpfOuCnpj()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma parte contrária ativa com este CPF/CNPJ");
        }

        ParteContraria novaParteContraria = new ParteContraria(
            request.getNome(),
            request.getCpfOuCnpj(),
            request.getTipoPessoa()
        );

        if (request.getEmail() != null) novaParteContraria.setEmail(request.getEmail());
        if (request.getTelefone() != null) novaParteContraria.setTelefone(request.getTelefone());
        if (request.getEndereco() != null) novaParteContraria.setEndereco(request.getEndereco());
        if (request.getCidade() != null) novaParteContraria.setCidade(request.getCidade());
        if (request.getEstado() != null) novaParteContraria.setEstado(request.getEstado());
        if (request.getCep() != null) novaParteContraria.setCep(request.getCep());
        if (request.getObservacoes() != null) novaParteContraria.setObservacoes(request.getObservacoes());

        ParteContraria parteSalva = parteContrariaRepositorio.salvar(novaParteContraria);
        return converterParaResponse(parteSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public ParteContrariaResponse consultarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepositorio.porId(id)
            .filter(ParteContraria::isAtiva)
            .orElse(null);
        return parteContraria != null ? converterParaResponse(parteContraria) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public ParteContrariaResponse consultarPorCpfOuCnpj(String cpfOuCnpj) {
        ParteContraria parteContraria = parteContrariaRepositorio.porCpfOuCnpjAtiva(cpfOuCnpj).orElse(null);
        return parteContraria != null ? converterParaResponse(parteContraria) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPartesContrarias() {
        return parteContrariaRepositorio.listarAtivas().stream()
            .map(this::converterParaResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarTodasAsPartesContrarias() {
        return parteContrariaRepositorio.listarTodas().stream()
            .map(this::converterParaResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPorTipo(TipoPessoa tipo) {
        return parteContrariaRepositorio.porTipoAtivas(tipo).stream()
            .map(this::converterParaResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParteContrariaResponse> listarPorCidade(String cidade) {
        return parteContrariaRepositorio.porCidadeAtivas(cidade).stream()
            .map(this::converterParaResponse).collect(Collectors.toList());
    }

    @Override
    public ParteContrariaResponse editarParteContraria(Long id, CriarParteContrariaRequest request) {
        ParteContraria parteContraria = parteContrariaRepositorio.porId(id)
            .orElseThrow(() -> new NoSuchElementException("Parte contrária não encontrada com ID: " + id));

        parteContraria.atualizar(
            request.getNome(),
            request.getEmail() != null ? request.getEmail() : parteContraria.getEmail(),
            request.getTelefone() != null ? request.getTelefone() : parteContraria.getTelefone(),
            request.getEndereco() != null ? request.getEndereco() : parteContraria.getEndereco(),
            request.getCidade() != null ? request.getCidade() : parteContraria.getCidade(),
            request.getEstado() != null ? request.getEstado() : parteContraria.getEstado(),
            request.getCep() != null ? request.getCep() : parteContraria.getCep(),
            request.getObservacoes() != null ? request.getObservacoes() : parteContraria.getObservacoes()
        );

        ParteContraria parteAtualizada = parteContrariaRepositorio.salvar(parteContraria);
        return converterParaResponse(parteAtualizada);
    }

    @Override
    public void deletarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepositorio.porId(id)
            .orElseThrow(() -> new NoSuchElementException("Parte contrária não encontrada com ID: " + id));
        parteContraria.desativar();
        parteContrariaRepositorio.salvar(parteContraria);
    }

    @Override
    public void reativarParteContraria(Long id) {
        ParteContraria parteContraria = parteContrariaRepositorio.porId(id)
            .orElseThrow(() -> new NoSuchElementException("Parte contrária não encontrada com ID: " + id));

        if (parteContraria.isAtiva()) {
            throw new IllegalStateException("Parte contrária já está ativa");
        }

        parteContraria.reativar();
        parteContrariaRepositorio.salvar(parteContraria);
    }

    private ParteContrariaResponse converterParaResponse(ParteContraria parteContraria) {
        return new ParteContrariaResponse(
            parteContraria.getId(), parteContraria.getNome(), parteContraria.getCpfOuCnpj(),
            parteContraria.getTipoPessoa(), parteContraria.getEmail(), parteContraria.getTelefone(),
            parteContraria.getEndereco(), parteContraria.getCidade(), parteContraria.getEstado(),
            parteContraria.getCep(), parteContraria.getObservacoes(), parteContraria.isAtiva(),
            parteContraria.getCriadaEm(), parteContraria.getAtualizadaEm()
        );
    }
}