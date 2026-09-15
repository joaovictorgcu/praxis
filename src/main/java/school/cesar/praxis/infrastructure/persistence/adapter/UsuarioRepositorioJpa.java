package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.infrastructure.persistence.entity.UsuarioEntity;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.UsuarioJpaRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Adaptador de saida: usuarios em JPA. */
@Repository
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

    private final UsuarioJpaRepository jpa;

    public UsuarioRepositorioJpa(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity salvo = jpa.save(PersistenciaMapper.paraEntidade(usuario));
        return PersistenciaMapper.paraDominio(salvo);
    }

    @Override
    public Optional<Usuario> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        return jpa.findByEmail(Usuario.normalizarEmail(email)).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Usuario> porOab(String oab) {
        return jpa.findByOab(oab.trim().toUpperCase(Locale.ROOT)).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Usuario> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
    }
}
