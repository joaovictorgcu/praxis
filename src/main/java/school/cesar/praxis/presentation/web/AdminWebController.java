package school.cesar.praxis.presentation.web;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import school.cesar.praxis.application.port.in.AdministracaoUseCases;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * Tela unica de administracao ({@code /painel/admin}), reservada ao chefe: o
 * retrato de tudo que o escritorio guardou, mais a ficha tecnica da instancia
 * em execucao. So le - cadastrar e remover continua sendo na tela de cada
 * cadastro, que e onde a regra vive.
 */
@SomenteChefe
@Controller
@RequestMapping("/painel/admin")
public class AdminWebController {

    private final AdministracaoUseCases.ConsultarPanorama panorama;
    private final NotificadorPainel avisos;
    private final Environment ambiente;

    public AdminWebController(AdministracaoUseCases.ConsultarPanorama panorama,
                              NotificadorPainel avisos,
                              Environment ambiente) {
        this.panorama = panorama;
        this.avisos = avisos;
        this.ambiente = ambiente;
    }

    @GetMapping
    public String admin(Model model) {
        model.addAttribute("panorama", panorama.executar());
        model.addAttribute("avisos", avisos.getEntregues());
        model.addAttribute("sistema", fichaTecnica());
        model.addAttribute("atalhos", Atalho.todos());
        model.addAttribute("hoje", LocalDate.now());
        return "admin";
    }

    /**
     * Ficha da instancia. A URL do banco vai sem a parte de parametros, que em
     * alguns drivers carrega credencial; usuario e senha nunca aparecem.
     */
    private FichaTecnica fichaTecnica() {
        String[] perfis = ambiente.getActiveProfiles();
        return new FichaTecnica(
                perfis.length == 0 ? "dev (nenhum perfil ativo)" : String.join(", ", perfis),
                semParametros(ambiente.getProperty("spring.datasource.url", "nao informado")),
                ambiente.getProperty("spring.jpa.hibernate.ddl-auto", "padrao"),
                Boolean.parseBoolean(ambiente.getProperty("spring.flyway.enabled", "false")),
                Boolean.parseBoolean(ambiente.getProperty("praxis.dados-exemplo", "false")),
                ambiente.getProperty("praxis.dominio-email", "-"),
                System.getProperty("java.version"),
                tempoNoAr());
    }

    private static String semParametros(String url) {
        int parametros = url.indexOf('?');
        return parametros < 0 ? url : url.substring(0, parametros);
    }

    private static String tempoNoAr() {
        Duration duracao = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        return duracao.toHours() + "h " + duracao.toMinutesPart() + "min " + duracao.toSecondsPart() + "s";
    }

    /** Dados do ambiente mostrados na tela; nao e regra de negocio, e diagnostico. */
    public record FichaTecnica(String perfis,
                               String bancoUrl,
                               String ddlAuto,
                               boolean flyway,
                               boolean dadosDeExemplo,
                               String dominioEmail,
                               String java,
                               String tempoNoAr) {
    }

    /** Atalhos de administracao que ja existem, agrupados em um lugar so. */
    public record Atalho(String titulo, String descricao, String caminho) {

        public static List<Atalho> todos() {
            return List.of(
                    new Atalho("Usuários", "Cadastrar, promover e remover quem acessa o sistema",
                            "/painel/usuarios"),
                    new Atalho("Modelos", "Modelos de peca usados na geracao de documentos",
                            "/painel/modelos"),
                    new Atalho("Feriados", "Calendario forense que alimenta a contagem de prazos",
                            "/painel/feriados"),
                    new Atalho("Processos", "Cadastro de processos, andamentos e prazos",
                            "/painel/processos"),
                    new Atalho("Documentos", "Fila de revisao, aprovacao e protocolo de pecas",
                            "/painel/documentos"),
                    new Atalho("Anexos", "Arquivos juntados aos autos", "/painel/anexos"));
        }
    }
}
