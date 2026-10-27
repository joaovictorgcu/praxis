package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.in.HonorariosUseCases.DetalheContrato;
import school.cesar.praxis.application.port.in.HonorariosUseCases.ItemContrato;
import school.cesar.praxis.application.port.in.HonorariosUseCases.ItemParcela;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.honorario.StatusParcela;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Camada de apresentacao do controle financeiro de honorarios: a lista com
 * totais e filtros, e o detalhe do contrato com parcelas, pagamento e
 * resultado da causa. Nao contem regra - so traduz formulario em caso de uso,
 * igual as demais telas do painel.
 */
@Controller
@RequestMapping("/painel/honorarios")
public class HonorariosWebController {

    private final HonorariosUseCases.ListarContratos listarContratos;
    private final HonorariosUseCases.ConsultarDetalheContrato consultarDetalhe;
    private final HonorariosUseCases.RegistrarPagamentoParcela registrarPagamento;
    private final HonorariosUseCases.RegistrarResultadoCausa registrarResultado;
    private final ProcessosUseCases.ListarProcessos listarProcessos;
    private final ProcessosUseCases.ConsultarProcesso consultarProcesso;

    public HonorariosWebController(HonorariosUseCases.ListarContratos listarContratos,
                                   HonorariosUseCases.ConsultarDetalheContrato consultarDetalhe,
                                   HonorariosUseCases.RegistrarPagamentoParcela registrarPagamento,
                                   HonorariosUseCases.RegistrarResultadoCausa registrarResultado,
                                   ProcessosUseCases.ListarProcessos listarProcessos,
                                   ProcessosUseCases.ConsultarProcesso consultarProcesso) {
        this.listarContratos = listarContratos;
        this.consultarDetalhe = consultarDetalhe;
        this.registrarPagamento = registrarPagamento;
        this.registrarResultado = registrarResultado;
        this.listarProcessos = listarProcessos;
        this.consultarProcesso = consultarProcesso;
    }

    /** Linha da tabela: junta o contrato com o cliente do processo (o contrato nao guarda isso). */
    public record LinhaContrato(ItemContrato contrato, String cliente) {
    }

    @GetMapping
    public String honorarios(@RequestParam(required = false) String status,
                             @RequestParam(required = false) String modalidade,
                             @RequestParam(required = false) String cliente,
                             Model model) {
        Map<String, Processo> processosPorNumero = listarProcessos.executar().stream()
                .collect(Collectors.toMap(p -> p.getNumero().valor(), p -> p));

        List<ItemContrato> todos = listarContratos.executar(null);
        String filtroCliente = cliente == null ? "" : cliente.trim().toLowerCase();

        List<LinhaContrato> linhas = todos.stream()
                .map(ct -> new LinhaContrato(ct, nomeCliente(processosPorNumero, ct.numeroProcesso())))
                .filter(l -> status == null || status.isBlank() || status.equals(l.contrato().status()))
                .filter(l -> modalidade == null || modalidade.isBlank() || modalidade.equals(l.contrato().modalidade()))
                .filter(l -> filtroCliente.isEmpty() || l.cliente().toLowerCase().contains(filtroCliente))
                .toList();

        // Totais sobre a carteira inteira (nao so a pagina filtrada): o encargo exige o detalhe, com as parcelas.
        BigDecimal aReceber = BigDecimal.ZERO;
        BigDecimal recebido = BigDecimal.ZERO;
        BigDecimal emAtrasoComEncargos = BigDecimal.ZERO;
        for (ItemContrato ct : todos) {
            DetalheContrato detalhe = consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(ct.id()));
            aReceber = aReceber.add(detalhe.saldoDevedor());
            recebido = recebido.add(detalhe.totalPago());
            emAtrasoComEncargos = emAtrasoComEncargos.add(detalhe.parcelas().stream()
                    .filter(p -> StatusParcela.EM_ATRASO.name().equals(p.status()))
                    .map(ItemParcela::valorAtualizado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        model.addAttribute("linhas", linhas);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("modalidadeFiltro", modalidade);
        model.addAttribute("clienteFiltro", cliente);
        model.addAttribute("aReceber", aReceber);
        model.addAttribute("recebido", recebido);
        model.addAttribute("emAtrasoComEncargos", emAtrasoComEncargos);
        model.addAttribute("temAtraso", emAtrasoComEncargos.compareTo(BigDecimal.ZERO) > 0);
        return "honorarios";
    }

    private static String nomeCliente(Map<String, Processo> processos, String numeroProcesso) {
        Processo processo = processos.get(numeroProcesso);
        return processo == null ? "-" : processo.getCliente();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, UsuarioLogado usuario, Model model) {
        try {
            DetalheContrato detalhe = consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(id));
            Processo processo = consultarProcesso.consultar(detalhe.numeroProcesso());
            model.addAttribute("detalhe", detalhe);
            model.addAttribute("processo", processo);
            model.addAttribute("podeAgir", podeAgir(usuario, processo));
            model.addAttribute("hoje", LocalDate.now());
            return "honorarios-detalhe";
        } catch (NoSuchElementException falha) {
            model.addAttribute("erro", falha.getMessage());
            return honorarios(null, null, null, model);
        }
    }

    /** So o chefe e o advogado responsavel do processo registram pagamento ou resultado; os demais so visualizam. */
    private static boolean podeAgir(UsuarioLogado usuario, Processo processo) {
        return usuario != null && (usuario.chefe() || processo.getResponsavel().oab().equalsIgnoreCase(usuario.oab()));
    }

    @PostMapping("/{id}/parcelas/{numero}/pagamentos")
    public String registrarPagamento(@PathVariable Long id, @PathVariable int numero,
                                     @RequestParam String valor,
                                     @RequestParam String dataPagamento,
                                     UsuarioLogado usuario, RedirectAttributes flash) {
        return executar(flash, usuario, id, "Pagamento registrado.", () ->
                registrarPagamento.executar(new HonorariosUseCases.RegistrarPagamentoParcela.Comando(
                        id, numero, new BigDecimal(valor), LocalDate.parse(dataPagamento))));
    }

    @PostMapping("/{id}/resultado")
    public String registrarResultado(@PathVariable Long id,
                                     @RequestParam String valorObtido,
                                     @RequestParam(required = false) String honorariosSucumbencia,
                                     UsuarioLogado usuario, RedirectAttributes flash) {
        BigDecimal sucumbencia = honorariosSucumbencia == null || honorariosSucumbencia.isBlank()
                ? BigDecimal.ZERO : new BigDecimal(honorariosSucumbencia);
        return executar(flash, usuario, id, "Resultado da causa registrado.", () ->
                registrarResultado.executar(new HonorariosUseCases.RegistrarResultadoCausa.Comando(
                        id, new BigDecimal(valorObtido), sucumbencia)));
    }

    private String executar(RedirectAttributes flash, UsuarioLogado usuario, Long id, String sucesso, Supplier<?> acao) {
        try {
            DetalheContrato atual = consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(id));
            Processo processo = consultarProcesso.consultar(atual.numeroProcesso());
            if (!podeAgir(usuario, processo)) {
                flash.addFlashAttribute("erro", "só o chefe ou o advogado responsável pelo processo pode registrar isso");
                return "redirect:/painel/honorarios/" + id;
            }
            acao.get();
            flash.addFlashAttribute("mensagem", sucesso);
        } catch (DateTimeParseException | NumberFormatException data) {
            flash.addFlashAttribute("erro", "valor ou data inválidos");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/honorarios/" + id;
    }
}
