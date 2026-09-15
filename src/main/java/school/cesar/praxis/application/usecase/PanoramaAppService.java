package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AdministracaoUseCases;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;

/**
 * Caso de uso de leitura: o panorama do escritorio. Nao fala com repositorio -
 * so reune o que cada caso de uso de listagem ja sabe responder, para a tela de
 * administracao carregar tudo em uma ida ao servidor.
 */
@Service
public class PanoramaAppService implements AdministracaoUseCases.ConsultarPanorama {

    private final UsuariosUseCases.ListarUsuarios usuarios;
    private final ProcessosUseCases.ListarProcessos processos;
    private final PrazosUseCases.ListarTodosOsPrazos prazos;
    private final DocumentosUseCases.ListarDocumentos documentos;
    private final AnexosUseCases.ListarAnexos anexos;
    private final ModelosUseCases.ListarModelos modelos;
    private final FeriadosUseCases.ListarFeriados feriados;
    private final HonorariosUseCases.ListarContratos contratos;
    private final ClienteUseCase clientes;
    private final ParteContrariaUseCase partesContrarias;
    private final AgendaDeAudienciasUseCase audiencias;

    public PanoramaAppService(UsuariosUseCases.ListarUsuarios usuarios,
                              ProcessosUseCases.ListarProcessos processos,
                              PrazosUseCases.ListarTodosOsPrazos prazos,
                              DocumentosUseCases.ListarDocumentos documentos,
                              AnexosUseCases.ListarAnexos anexos,
                              ModelosUseCases.ListarModelos modelos,
                              FeriadosUseCases.ListarFeriados feriados,
                              HonorariosUseCases.ListarContratos contratos,
                              ClienteUseCase clientes,
                              ParteContrariaUseCase partesContrarias,
                              AgendaDeAudienciasUseCase audiencias) {
        this.usuarios = usuarios;
        this.processos = processos;
        this.prazos = prazos;
        this.documentos = documentos;
        this.anexos = anexos;
        this.modelos = modelos;
        this.feriados = feriados;
        this.contratos = contratos;
        this.clientes = clientes;
        this.partesContrarias = partesContrarias;
        this.audiencias = audiencias;
    }

    @Override
    @Transactional(readOnly = true)
    public Panorama executar() {
        return new Panorama(
                usuarios.executar(),
                processos.executar(),
                prazos.executar(),
                documentos.executar(null),
                anexos.executar(null),
                modelos.executar(),
                feriados.executar(),
                contratos.executar(null),
                clientes.listarTodosOsClientes(),
                partesContrarias.listarTodasAsPartesContrarias(),
                audiencias.listarTodasAsAudiencias());
    }
}
