package school.cesar.praxis.presentation.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;
import school.cesar.praxis.domain.agenda.HorarioInvalidoException;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@SomenteChefe
@Controller
@RequestMapping("/painel/cadastros")
public class CadastrosRelacionadosWebController {

    private final ClienteUseCase clientes;
    private final ParteContrariaUseCase partesContrarias;
    private final AgendaDeAudienciasUseCase audiencias;

    public CadastrosRelacionadosWebController(ClienteUseCase clientes,
                                              ParteContrariaUseCase partesContrarias,
                                              AgendaDeAudienciasUseCase audiencias) {
        this.clientes = clientes;
        this.partesContrarias = partesContrarias;
        this.audiencias = audiencias;
    }

    @GetMapping("/clientes")
    public String clientes(@RequestParam(required = false) Long editar, Model model) {
        var lista = clientes.listarTodosOsClientes();
        model.addAttribute("clientes", lista);
        model.addAttribute("tiposPessoa", TipoPessoa.values());
        model.addAttribute("clienteEdicao", lista.stream()
                .filter(cliente -> cliente.getId().equals(editar) && cliente.isAtivo())
                .findFirst().orElse(null));
        return "cadastro-clientes";
    }

    @PostMapping("/clientes")
    public String criarCliente(@RequestParam String nome,
                               @RequestParam String cpfOuCnpj,
                               @RequestParam TipoPessoa tipoPessoa,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String telefone,
                               @RequestParam(required = false) String celular,
                               @RequestParam(required = false) String endereco,
                               @RequestParam(required = false) String cidade,
                               @RequestParam(required = false) String estado,
                               @RequestParam(required = false) String cep,
                               @RequestParam(required = false) String profissao,
                               @RequestParam(required = false) String empresaTrabalho,
                               @RequestParam(required = false) String observacoes,
                               RedirectAttributes flash) {
        try {
            CriarClienteRequest request = new CriarClienteRequest(nome, cpfOuCnpj, tipoPessoa);
            request.setEmail(email);
            request.setTelefone(telefone);
            request.setCelular(celular);
            request.setEndereco(endereco);
            request.setCidade(cidade);
            request.setEstado(estado);
            request.setCep(cep);
            request.setProfissao(profissao);
            request.setEmpresaTrabalho(empresaTrabalho);
            request.setObservacoes(observacoes);
            clientes.criarCliente(request);
            flash.addFlashAttribute("mensagem", "Cliente cadastrado.");
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/clientes";
    }

    @PostMapping("/clientes/{id}/editar")
    public String editarCliente(@PathVariable Long id,
                                @RequestParam String nome,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String telefone,
                                @RequestParam(required = false) String celular,
                                @RequestParam(required = false) String endereco,
                                @RequestParam(required = false) String cidade,
                                @RequestParam(required = false) String estado,
                                @RequestParam(required = false) String cep,
                                @RequestParam(required = false) String profissao,
                                @RequestParam(required = false) String empresaTrabalho,
                                @RequestParam(required = false) String observacoes,
                                RedirectAttributes flash) {
        try {
            CriarClienteRequest request = new CriarClienteRequest();
            request.setNome(nome);
            request.setEmail(email);
            request.setTelefone(telefone);
            request.setCelular(celular);
            request.setEndereco(endereco);
            request.setCidade(cidade);
            request.setEstado(estado);
            request.setCep(cep);
            request.setProfissao(profissao);
            request.setEmpresaTrabalho(empresaTrabalho);
            request.setObservacoes(observacoes);
            clientes.editarCliente(id, request);
            flash.addFlashAttribute("mensagem", "Cliente atualizado.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/clientes";
    }

    @PostMapping("/clientes/{id}/desativar")
    public String desativarCliente(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clientes.deletarCliente(id);
            flash.addFlashAttribute("mensagem", "Cliente desativado.");
        } catch (NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/clientes";
    }

    @PostMapping("/clientes/{id}/reativar")
    public String reativarCliente(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clientes.reativarCliente(id);
            flash.addFlashAttribute("mensagem", "Cliente reativado.");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/clientes";
    }

    @GetMapping("/partes-contrarias")
    public String partesContrarias(@RequestParam(required = false) Long editar, Model model) {
        var lista = partesContrarias.listarTodasAsPartesContrarias();
        model.addAttribute("partes", lista);
        model.addAttribute("tiposPessoa", TipoPessoa.values());
        model.addAttribute("parteEdicao", lista.stream()
                .filter(parte -> parte.getId().equals(editar) && parte.isAtiva())
                .findFirst().orElse(null));
        return "cadastro-partes-contrarias";
    }

    @PostMapping("/partes-contrarias")
    public String criarParteContraria(@RequestParam String nome,
                                      @RequestParam String cpfOuCnpj,
                                      @RequestParam TipoPessoa tipoPessoa,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String telefone,
                                      @RequestParam(required = false) String endereco,
                                      @RequestParam(required = false) String cidade,
                                      @RequestParam(required = false) String estado,
                                      @RequestParam(required = false) String cep,
                                      @RequestParam(required = false) String observacoes,
                                      RedirectAttributes flash) {
        try {
            CriarParteContrariaRequest request =
                    new CriarParteContrariaRequest(nome, cpfOuCnpj, tipoPessoa);
            request.setEmail(email);
            request.setTelefone(telefone);
            request.setEndereco(endereco);
            request.setCidade(cidade);
            request.setEstado(estado);
            request.setCep(cep);
            request.setObservacoes(observacoes);
            partesContrarias.criarParteContraria(request);
            flash.addFlashAttribute("mensagem", "Parte contrária cadastrada.");
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/partes-contrarias";
    }

    @PostMapping("/partes-contrarias/{id}/editar")
    public String editarParteContraria(@PathVariable Long id,
                                       @RequestParam String nome,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String telefone,
                                       @RequestParam(required = false) String endereco,
                                       @RequestParam(required = false) String cidade,
                                       @RequestParam(required = false) String estado,
                                       @RequestParam(required = false) String cep,
                                       @RequestParam(required = false) String observacoes,
                                       RedirectAttributes flash) {
        try {
            CriarParteContrariaRequest request = new CriarParteContrariaRequest();
            request.setNome(nome);
            request.setEmail(email);
            request.setTelefone(telefone);
            request.setEndereco(endereco);
            request.setCidade(cidade);
            request.setEstado(estado);
            request.setCep(cep);
            request.setObservacoes(observacoes);
            partesContrarias.editarParteContraria(id, request);
            flash.addFlashAttribute("mensagem", "Parte contrária atualizada.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/partes-contrarias";
    }

    @PostMapping("/partes-contrarias/{id}/desativar")
    public String desativarParteContraria(@PathVariable Long id, RedirectAttributes flash) {
        try {
            partesContrarias.deletarParteContraria(id);
            flash.addFlashAttribute("mensagem", "Parte contrária desativada.");
        } catch (NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/partes-contrarias";
    }

    @PostMapping("/partes-contrarias/{id}/reativar")
    public String reativarParteContraria(@PathVariable Long id, RedirectAttributes flash) {
        try {
            partesContrarias.reativarParteContraria(id);
            flash.addFlashAttribute("mensagem", "Parte contrária reativada.");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/partes-contrarias";
    }

    @GetMapping("/audiencias")
    public String audiencias(@RequestParam(required = false) Long editar, Model model) {
        var lista = audiencias.listarTodasAsAudiencias();
        model.addAttribute("audiencias", lista);
        model.addAttribute("audienciaEdicao", lista.stream()
                .filter(audiencia -> audiencia.getId().equals(editar) && audiencia.isAtiva())
                .findFirst().orElse(null));
        return "cadastro-audiencias";
    }

    @PostMapping("/audiencias")
    public String criarAudiencia(@RequestParam String numeroProcesso,
                                 @RequestParam String nomeParteAutora,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 LocalDateTime dataHoraInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 LocalDateTime dataHoraFim,
                                 @RequestParam String sala,
                                 @RequestParam(required = false) String observacoes,
                                 RedirectAttributes flash) {
        try {
            audiencias.criarAudiencia(new CriarAudienciaRequest(
                    numeroProcesso, nomeParteAutora, dataHoraInicio, dataHoraFim, sala, observacoes));
            flash.addFlashAttribute("mensagem", "Audiência agendada.");
        } catch (IllegalArgumentException | ConflitoDEAudienciaException | HorarioInvalidoException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/audiencias";
    }

    @PostMapping("/audiencias/{id}/editar")
    public String editarAudiencia(@PathVariable Long id,
                                  @RequestParam String nomeParteAutora,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataHoraInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataHoraFim,
                                  @RequestParam String sala,
                                  @RequestParam(required = false) String observacoes,
                                  RedirectAttributes flash) {
        try {
            audiencias.editarAudiencia(id, new CriarAudienciaRequest(
                    null, nomeParteAutora, dataHoraInicio, dataHoraFim, sala, observacoes));
            flash.addFlashAttribute("mensagem", "Audiência atualizada.");
        } catch (IllegalArgumentException | ConflitoDEAudienciaException | HorarioInvalidoException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/audiencias";
    }

    @PostMapping("/audiencias/{id}/cancelar")
    public String cancelarAudiencia(@PathVariable Long id, RedirectAttributes flash) {
        try {
            audiencias.deletarAudiencia(id);
            flash.addFlashAttribute("mensagem", "Audiência cancelada.");
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/audiencias";
    }

    @PostMapping("/audiencias/{id}/reativar")
    public String reativarAudiencia(@PathVariable Long id, RedirectAttributes flash) {
        try {
            audiencias.reativarAudiencia(id);
            flash.addFlashAttribute("mensagem", "Audiência reativada.");
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/cadastros/audiencias";
    }
}
