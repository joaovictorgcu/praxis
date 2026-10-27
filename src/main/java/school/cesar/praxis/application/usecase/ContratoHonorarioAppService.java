package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.out.ContratoHonorarioRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ContratoHonorarioAppService implements HonorariosUseCases.CadastrarContrato,
        HonorariosUseCases.ListarContratos,
        HonorariosUseCases.ConsultarContrato,
        HonorariosUseCases.ConsultarDetalheContrato,
        HonorariosUseCases.RegistrarPagamentoParcela,
        HonorariosUseCases.RegistrarResultadoCausa {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ProcessoRepositorio processos;
    private final ContratoHonorarioRepositorio contratos;
    private final MotorDeHonorarios motor;
    private final Relogio relogio;

    public ContratoHonorarioAppService(ProcessoRepositorio processos,
                                       ContratoHonorarioRepositorio contratos,
                                       MotorDeHonorarios motor,
                                       Relogio relogio) {
        this.processos = processos;
        this.contratos = contratos;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public HonorariosUseCases.ItemContrato executar(HonorariosUseCases.CadastrarContrato.Comando comando) {
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
                numero, comando.celebradoEm(), base, calculo,
                comando.quantidadeParcelas(), comando.primeiroVencimento());

        return paraItem(contratos.salvar(contrato), relogio.hoje());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HonorariosUseCases.ItemContrato> executar(String numeroProcesso) {
        List<ContratoHonorario> encontrados = numeroProcesso == null || numeroProcesso.isBlank()
                ? contratos.listar()
                : contratos.porProcesso(NumeroCnj.de(numeroProcesso));

        var hoje = relogio.hoje();
        return encontrados.stream().map(contrato -> paraItem(contrato, hoje)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HonorariosUseCases.ItemContrato executar(Long id) {
        return paraItem(buscar(id), relogio.hoje());
    }

    @Override
    @Transactional(readOnly = true)
    public HonorariosUseCases.DetalheContrato executar(HonorariosUseCases.ConsultarDetalheContrato.Comando comando) {
        return paraDetalhe(buscar(comando.contratoId()), relogio.hoje());
    }

    @Override
    @Transactional
    public HonorariosUseCases.DetalheContrato executar(HonorariosUseCases.RegistrarPagamentoParcela.Comando comando) {
        ContratoHonorario contrato = buscar(comando.contratoId());
        contrato.registrarPagamento(comando.numeroParcela(), comando.valor(), comando.dataPagamento());
        return paraDetalhe(contratos.salvar(contrato), relogio.hoje());
    }

    @Override
    @Transactional
    public HonorariosUseCases.DetalheContrato executar(HonorariosUseCases.RegistrarResultadoCausa.Comando comando) {
        ContratoHonorario contrato = buscar(comando.contratoId());
        CalculoHonorarioStrategy calculo = motor.estrategiaPara(contrato.getModalidade());
        contrato.registrarResultado(comando.valorObtido(), comando.honorariosSucumbencia(), calculo);
        return paraDetalhe(contratos.salvar(contrato), relogio.hoje());
    }

    private ContratoHonorario buscar(Long id) {
        return contratos.porId(id)
                .orElseThrow(() -> new NoSuchElementException("contrato de honorario nao encontrado: " + id));
    }

    private static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private static HonorariosUseCases.ItemContrato paraItem(ContratoHonorario contrato, java.time.LocalDate hoje) {
        return new HonorariosUseCases.ItemContrato(
                contrato.getId(),
                contrato.getNumeroProcesso().valor(),
                contrato.getModalidade(),
                contrato.getCelebradoEm(),
                contrato.getValorContratado(),
                contrato.status(hoje).name(),
                contrato.aguardandoResultado(),
                contrato.proximaParcelaEmAberto(hoje).map(Parcela::getVencimento).orElse(null));
    }

    private static HonorariosUseCases.DetalheContrato paraDetalhe(ContratoHonorario contrato, java.time.LocalDate hoje) {
        List<HonorariosUseCases.ItemParcela> parcelas = contrato.getParcelas().stream()
                .map(p -> new HonorariosUseCases.ItemParcela(
                        p.getNumero(), p.getVencimento(), p.getValor(), p.getValorPago(), p.getDataPagamento(),
                        p.status(hoje).name(), p.multa(hoje), p.juros(hoje), p.valorAtualizado(hoje)))
                .toList();
        List<HonorariosUseCases.ItemHistorico> historico = contrato.getHistorico().stream()
                .map(r -> new HonorariosUseCases.ItemHistorico(r.getTipo(), r.getDescricao(), r.getQuando()))
                .toList();
        return new HonorariosUseCases.DetalheContrato(
                contrato.getId(),
                contrato.getNumeroProcesso().valor(),
                contrato.getModalidade(),
                contrato.getCelebradoEm(),
                contrato.getValorContratado(),
                contrato.status(hoje).name(),
                contrato.aguardandoResultado(),
                contrato.getQuantidadeParcelas(),
                contrato.getPrimeiroVencimento(),
                contrato.getBaseCalculo().valorFixo(),
                contrato.getBaseCalculo().valorHora(),
                contrato.getBaseCalculo().horasTrabalhadas(),
                contrato.getBaseCalculo().valorCausa(),
                contrato.getBaseCalculo().percentualExito(),
                contrato.getValorObtido(),
                contrato.getHonorariosSucumbencia(),
                contrato.totalPago(),
                contrato.saldoDevedor(),
                parcelas,
                historico);
    }
}
