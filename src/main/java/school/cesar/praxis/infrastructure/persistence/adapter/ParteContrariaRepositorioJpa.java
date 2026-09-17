package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ParteContrariaRepositorio;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.domain.partecontraria.ParteContraria;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ParteContrariaJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida: partes contrarias em JPA. */
@Repository
public class ParteContrariaRepositorioJpa implements ParteContrariaRepositorio {

    private final ParteContrariaJpaRepository jpa;

    public ParteContrariaRepositorioJpa(ParteContrariaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ParteContraria salvar(ParteContraria parteContraria) {
        return PersistenciaMapper.paraDominio(
                jpa.save(PersistenciaMapper.paraEntidade(parteContraria)));
    }

    @Override
    public Optional<ParteContraria> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<ParteContraria> porCpfOuCnpjAtiva(String cpfOuCnpj) {
        return jpa.findByCpfOuCnpjAndAtivaTrue(cpfOuCnpj).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<ParteContraria> listarAtivas() {
        return jpa.findByAtivaTrue().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<ParteContraria> listarTodas() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<ParteContraria> porTipoAtivas(TipoPessoa tipo) {
        return jpa.findByTipoPessoaAndAtivaTrue(tipo).stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<ParteContraria> porCidadeAtivas(String cidade) {
        return jpa.findByCidadeAndAtivaTrue(cidade).stream().map(PersistenciaMapper::paraDominio).toList();
    }
}