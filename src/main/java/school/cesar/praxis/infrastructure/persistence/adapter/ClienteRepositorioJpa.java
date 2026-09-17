package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ClienteRepositorio;
import school.cesar.praxis.domain.cliente.Cliente;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ClienteJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida: clientes em JPA. */
@Repository
public class ClienteRepositorioJpa implements ClienteRepositorio {

    private final ClienteJpaRepository jpa;

    public ClienteRepositorioJpa(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return PersistenciaMapper.paraDominio(
                jpa.save(PersistenciaMapper.paraEntidade(cliente)));
    }

    @Override
    public Optional<Cliente> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Cliente> porCpfOuCnpj(String cpfOuCnpj) {
        return jpa.findByCpfOuCnpj(cpfOuCnpj).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Cliente> porCpfOuCnpjAtivo(String cpfOuCnpj) {
        return jpa.findByCpfOuCnpjAndAtivoTrue(cpfOuCnpj).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Cliente> listarAtivos() {
        return jpa.findByAtivoTrue().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Cliente> porTipoAtivos(TipoPessoa tipo) {
        return jpa.findByTipoPessoaAndAtivoTrue(tipo).stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Cliente> porCidadeAtivos(String cidade) {
        return jpa.findByCidadeAndAtivoTrue(cidade).stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Cliente> porEstadoAtivos(String estado) {
        return jpa.findByEstadoAndAtivoTrue(estado).stream().map(PersistenciaMapper::paraDominio).toList();
    }
}