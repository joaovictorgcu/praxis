package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.out.ContratoHonorarioRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.honorario.BaseCalculo;
import school.cesar.praxis.domain.honorario.CalculoHonorarioStrategy;
import school.cesar.praxis.domain.honorario.ContratoHonorario;
import school.cesar.praxis.domain.honorario.MotorDeHonorarios;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ContratoHonorarioAppService implements HonorariosUseCases.CadastrarContrato,
        HonorariosUseCases.ListarContratos,
        HonorariosUseCases.ConsultarContrato {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ProcessoRepositorio processos;
    private final ContratoHonorarioRepositorio contratos;
    private final MotorDeHonorarios motor;

    public ContratoHonorarioAppService(ProcessoRepositorio processos,
                                       ContratoHonorarioRepositorio contratos,
                                       MotorDeHonorarios motor) {
        this.processos = processos;
        this.contratos = contratos;
        this.motor = motor;
    }

    @Override
    @Transactional
    public HonorariosUseCases.ItemContrato executar(Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo nao encontrado: " + comando.numeroProcesso()));

        CalculoHonorarioStrategy calculo = motor.estrategiaPara(comando.modalidade());

        BaseCalculo base = new BaseCalculo(
                nvl(comando.valorFixo()),
                nvl(comando.valorHora()),
                comando.horasTrabalhadas(),
                nvl(comando.valorCausa()),
                nvl(comando.percentualExito()));

        ContratoHonorario contrato = new ContratoHonorario(
                numero, comando.celebradoEm(), base, calculo);

        return paraItem(contratos.salvar(contrato));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HonorariosUseCases.ItemContrato> executar(String numeroProcesso) {
        List<ContratoHonorario> encontrados = numeroProcesso == null || numeroProcesso.isBlank()
                ? contratos.listar()
                : contratos.porProcesso(NumeroCnj.de(numeroProcesso));

        return encontrados.stream().map(ContratoHonorarioAppService::paraItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HonorariosUseCases.ItemContrato executar(Long id) {
        return contratos.porId(id)
                .map(ContratoHonorarioAppService::paraItem)
                .orElseThrow(() -> new NoSuchElementException("contrato de honorario nao encontrado: " + id));
    }

    private static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private static HonorariosUseCases.ItemContrato paraItem(ContratoHonorario contrato) {
        return new HonorariosUseCases.ItemContrato(
                contrato.getId(),
                contrato.getNumeroProcesso().valor(),
                contrato.getModalidade(),
                contrato.getCelebradoEm(),
                contrato.getValorContratado());
    }
}
