package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.FeriadoRepositorio;
import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.FeriadoJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de saida: cadastro de feriados em JPA.
 *
 * <p>Mantem a lista em memoria porque o calendario pergunta dia a dia ao
 * percorrer um prazo: sem isso, contar 15 dias uteis viraria 15 consultas, e a
 * varredura diaria multiplicaria isso pelo numero de prazos em aberto. Toda
 * escrita descarta o cache, entao o feriado cadastrado ja vale na contagem
 * seguinte. Suficiente para instancia unica, nao para escala horizontal.
 */
@Repository
public class FeriadoRepositorioJpa implements FeriadoRepositorio {

    private final FeriadoJpaRepository jpa;
    private volatile List<Feriado> cache;

    public FeriadoRepositorioJpa(FeriadoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Feriado> vigentes() {
        List<Feriado> atual = cache;
        if (atual == null) {
            atual = jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
            cache = atual;
        }
        return atual;
    }

    @Override
    public Feriado salvar(Feriado feriado) {
        Feriado salvo = PersistenciaMapper.paraDominio(
                jpa.save(PersistenciaMapper.paraEntidade(feriado)));
        cache = null;
        return salvo;
    }

    @Override
    public Optional<Feriado> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
        cache = null;
    }
}
