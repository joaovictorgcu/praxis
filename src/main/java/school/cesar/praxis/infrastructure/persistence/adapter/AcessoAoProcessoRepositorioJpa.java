package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.AcessoAoProcessoRepositorio;
import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.AcessoAoProcessoJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de saida das concessoes de acesso ao processo. Tambem e quem
 * responde, pela infraestrutura, se uma OAB tem acesso ativo a um processo -
 * o {@link VerificadorDeAcessoAoProcesso} que o Proxy usa (via
 * {@code PoliticaDeLeituraDosAutos}) sem conhecer o banco.
 */
@Repository
public class AcessoAoProcessoRepositorioJpa implements AcessoAoProcessoRepositorio, VerificadorDeAcessoAoProcesso {

    private final AcessoAoProcessoJpaRepository jpa;

    public AcessoAoProcessoRepositorioJpa(AcessoAoProcessoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AcessoAoProcesso salvar(AcessoAoProcesso acesso) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(acesso)));
    }

    @Override
    public Optional<AcessoAoProcesso> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<AcessoAoProcesso> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcessoOrderByIdDesc(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public boolean possuiAcessoAtivo(NumeroCnj processo, String oab, LocalDate hoje) {
        return jpa.findByNumeroProcessoOrderByIdDesc(processo.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .anyMatch(a -> a.getOab().equals(oab) && a.ativo(hoje));
    }
}
