package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.*;
import school.cesar.praxis.domain.advogado.StatusAdvogado;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.processo.TipoAndamento;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Camada de apresentacao web do subdominio nuclear: lista e cadastro de
 * processos, e a ficha do processo (linha do tempo, prazos, pecas, anexos),
 * de onde se registra andamento e se abre prazo. Nao contem regra - so traduz
 * formulario em caso de uso.
 */
@Controller
@RequestMapping("/painel/processos")
public class ProcessoWebController {

    private final ProcessosUseCases.ListarProcessos listar;
    private final ProcessosUseCases.ConsultarProcesso consultar;
    private final ProcessosUseCases.CadastrarProcesso cadastrar;
    private final ProcessosUseCases.RegistrarAndamento registrarAndamento;
    private final PrazosUseCases.AbrirPrazo abrirPrazo;
    private final PrazosUseCases.ConsultarPrazosDoProcesso prazosDoProcesso;
    private final PrazosUseCases.CumprirPrazo cumprirPrazo;
    private final DocumentosUseCases.ListarDocumentos listarDocumentos;
    private final AnexosUseCases.ListarAnexos listarAnexos;
    private final AdvogadosUseCases.ListarAdvogados listarAdvogados;
    private final HonorariosUseCases.CadastrarContrato cadastrarContrato;
    private final HonorariosUseCases.ListarContratos listarContratos;
    private final AcessoUseCases.ListarAcessosDoProcesso listarAcessos;
    private final AcessoUseCases.ListarSolicitacoes listarSolicitacoes;

    public ProcessoWebController(ProcessosUseCases.ListarProcessos listar,
                                 ProcessosUseCases.ConsultarProcesso consultar,
                                 ProcessosUseCases.CadastrarProcesso cadastrar,
                                 ProcessosUseCases.RegistrarAndamento registrarAndamento,
                                 PrazosUseCases.AbrirPrazo abrirPrazo,
                                 PrazosUseCases.ConsultarPrazosDoProcesso prazosDoProcesso,
                                 PrazosUseCases.CumprirPrazo cumprirPrazo,
                                 DocumentosUseCases.ListarDocumentos listarDocumentos,
                                 AnexosUseCases.ListarAnexos listarAnexos,
                                 AdvogadosUseCases.ListarAdvogados listarAdvogados,
                                 HonorariosUseCases.CadastrarContrato cadastrarContrato,
                                 HonorariosUseCases.ListarContratos listarContratos,
                                 AcessoUseCases.ListarAcessosDoProcesso listarAcessos,
                                 AcessoUseCases.ListarSolicitacoes listarSolicitacoes) {
        this.listar = listar;
        this.consultar = consultar;
        this.cadastrar = cadastrar;
        this.registrarAndamento = registrarAndamento;
        this.abrirPrazo = abrirPrazo;
        this.prazosDoProcesso = prazosDoProcesso;
        this.cumprirPrazo = cumprirPrazo;
        this.listarDocumentos = listarDocumentos;
        this.listarAnexos = listarAnexos;
        this.listarAdvogados = listarAdvogados;
        this.cadastrarContrato = cadastrarContrato;
        this.listarContratos = listarContratos;
        this.listarAcessos = listarAcessos;
        this.listarSolicitacoes = listarSolicitacoes;
    }

    /** So advogados ativos assumem processo novo; desativado fica so no historico. */
    private List<AdvogadosUseCases.ItemAdvogado> advogadosDisponiveisParaResponsavel() {
        return listarAdvogados.executar().stream()
                .filter(a -> a.status() == StatusAdvogado.ATIVO)
                .toList();
    }

    @GetMapping
    public String processos(@RequestParam(required = false) String busca, Model model) {
        String filtro = busca == null ? "" : busca.trim().toLowerCase();
        model.addAttribute("busca", busca);
        model.addAttribute("processos", listar.executar().stream()
                .filter(p -> filtro.isEmpty()
                        || p.getNumero().valor().contains(filtro)
                        || p.getCliente().toLowerCase().contains(filtro)
                        || p.getResponsavel().nome().toLowerCase().contains(filtro))
                .toList());
        model.addAttribute("advogados", advogadosDisponiveisParaResponsavel());
        return "processos";
    }

    /** O responsavel e escolhido entre os advogados cadastrados no escritorio: nome, e-mail e OAB vem de la. */
    @PostMapping
    public String cadastrar(@RequestParam String numeroCnj,
                            @RequestParam String cliente,
                            @RequestParam String comarca,
                            @RequestParam(required = false) Boolean segredoJustica,
                            @RequestParam String responsavelOab,
                            RedirectAttributes flash) {
        try {
            AdvogadosUseCases.ItemAdvogado responsavel = advogadosDisponiveisParaResponsavel().stream()
                    .filter(a -> a.oab().equalsIgnoreCase(responsavelOab))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "responsável com OAB " + responsavelOab + " não é advogado ativo do escritório"));
            Processo processo = cadastrar.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                    numeroCnj.trim(), cliente, comarca, Boolean.TRUE.equals(segredoJustica),
                    responsavel.nome(), responsavel.email(), responsavel.oab()));
            flash.addFlashAttribute("mensagem", "Processo " + processo.getNumero().valor() + " cadastrado.");
            return "redirect:/painel/processos/" + processo.getNumero().valor();
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
            return "redirect:/painel/processos";
        } catch (org.springframework.dao.DataIntegrityViolationException duplicado) {
            flash.addFlashAttribute("erro", "já existe processo com o número " + numeroCnj);
            return "redirect:/painel/processos";
        }
    }

    @GetMapping("/{numero}")
    public String ficha(@PathVariable String numero, UsuarioLogado usuario, Model model) {
        try {
            Processo processo = consultar.consultar(numero);
            model.addAttribute("processo", processo);
            model.addAttribute("andamentos", processo); // Iterable: linha do tempo cronologica
            model.addAttribute("prazos", prazosDoProcesso.executar(numero));
            model.addAttribute("documentos", listarDocumentos.executar(numero));
            model.addAttribute("anexos", listarAnexos.executar(numero));
            model.addAttribute("contratos", listarContratos.executar(numero));
            model.addAttribute("tiposAndamento", TipoAndamento.values());
            model.addAttribute("regimes", RegimeContagem.values());
            model.addAttribute("hoje", LocalDate.now());

            List<AcessoUseCases.ItemAcesso> acessos = listarAcessos.executar(numero);
            boolean possuiAcessoAtivo = acessos.stream()
                    .anyMatch(a -> a.oab().equalsIgnoreCase(usuario.oab()) && a.ativo());
            boolean podeLer = !processo.isSegredoJustica()
                    || processo.getResponsavel().oab().equalsIgnoreCase(usuario.oab())
                    || possuiAcessoAtivo;
            boolean temSolicitacaoPendente = listarSolicitacoes
                    .executar(new AcessoUseCases.ListarSolicitacoes.Comando(usuario.oab())).stream()
                    .anyMatch(s -> s.numeroProcesso().equals(numero) && "PENDENTE".equals(s.status()));
            model.addAttribute("podeLer", podeLer);
            model.addAttribute("podeSolicitarAcesso",
                    processo.isSegredoJustica() && !podeLer && !temSolicitacaoPendente);
            return "processo";
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            model.addAttribute("erro", falha.getMessage());
            model.addAttribute("processos", listar.executar());
            model.addAttribute("advogados", advogadosDisponiveisParaResponsavel());
            return "processos";
        }
    }

    @PostMapping("/{numero}/andamentos")
    public String registrarAndamento(@PathVariable String numero,
                                     @RequestParam String data,
                                     @RequestParam String descricao,
                                     @RequestParam TipoAndamento tipo,
                                     RedirectAttributes flash) {
        return executar(flash, numero, "Andamento registrado.", () ->
                registrarAndamento.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                        numero, LocalDate.parse(data), descricao, tipo)));
    }

    @PostMapping("/{numero}/prazos")
    public String abrirPrazo(@PathVariable String numero,
                             @RequestParam String descricao,
                             @RequestParam String intimacao,
                             @RequestParam int quantidadeDias,
                             @RequestParam(required = false) Boolean fatal,
                             @RequestParam RegimeContagem regime,
                             RedirectAttributes flash) {
        return executar(flash, numero, "Prazo aberto; vencimento calculado pelo motor de prazos.", () ->
                abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                        numero, descricao, LocalDate.parse(intimacao), quantidadeDias,
                        Boolean.TRUE.equals(fatal), regime)));
    }

    @PostMapping("/{numero}/honorarios")
    public String contratarHonorario(@PathVariable String numero,
                                     @RequestParam String modalidade,
                                     @RequestParam String celebradoEm,
                                     @RequestParam(required = false) BigDecimal valorFixo,
                                     @RequestParam(required = false) BigDecimal valorHora,
                                     @RequestParam(required = false) Integer horasTrabalhadas,
                                     @RequestParam(required = false) BigDecimal valorCausa,
                                     @RequestParam(required = false) BigDecimal percentualExito,
                                     @RequestParam int quantidadeParcelas,
                                     @RequestParam String primeiroVencimento,
                                     RedirectAttributes flash) {
        return executar(flash, numero, "Contrato de honorários celebrado.", () ->
                cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                        numero, modalidade, LocalDate.parse(celebradoEm),
                        valorFixo, valorHora, horasTrabalhadas == null ? 0 : horasTrabalhadas,
                        valorCausa, percentualExito,
                        quantidadeParcelas, LocalDate.parse(primeiroVencimento))));
    }

    @PostMapping("/{numero}/prazos/{id}/cumprir")
    public String cumprir(@PathVariable String numero, @PathVariable Long id, RedirectAttributes flash) {
        return executar(flash, numero, "Prazo registrado como cumprido.", () -> cumprirPrazo.executar(id));
    }

    private static String executar(RedirectAttributes flash, String numero, String sucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("mensagem", sucesso);
        } catch (DateTimeParseException data) {
            flash.addFlashAttribute("erro", "data inválida: " + data.getParsedString());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/processos/" + numero;
    }
}
