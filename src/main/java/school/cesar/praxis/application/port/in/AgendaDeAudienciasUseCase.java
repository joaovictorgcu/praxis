package school.cesar.praxis.application.port.in;

import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Port (Interface de Entrada) que define os casos de uso da Agenda de Audiências.
 * Segue o padrão de Arquitetura Limpa: a aplicação depende dessa interface.
 */
public interface AgendaDeAudienciasUseCase {

    /**
     * Caso de uso: Criar uma nova audiência
     * 
     * @param request Dados da audiência a ser criada
     * @return Audiência criada
     * @throws school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException Se houver conflito de horário
     */
    AudienciaResponse criarAudiencia(CriarAudienciaRequest request);

    /**
     * Caso de uso: Consultar audiência por ID
     * 
     * @param id ID da audiência
     * @return Audiência encontrada ou null
     */
    AudienciaResponse consultarAudiencia(Long id);

    /**
     * Caso de uso: Consultar audiência por número de processo
     * 
     * @param numeroProcesso Número do processo
     * @return Audiência encontrada ou null
     */
    AudienciaResponse consultarAudienciaPorProcesso(String numeroProcesso);

    /**
     * Caso de uso: Listar todas as audiências ativas
     * 
     * @return Lista de audiências
     */
    List<AudienciaResponse> listarAudiencias();

    /**
     * Caso de uso: Listar audiências por sala
     * 
     * @param sala Nome da sala
     * @return Lista de audiências da sala
     */
    List<AudienciaResponse> listarAudienciasPorSala(String sala);

    /**
     * Caso de uso: Listar audiências dentro de um período
     * 
     * @param dataInicio Data/hora inicial
     * @param dataFim Data/hora final
     * @return Lista de audiências no período
     */
    List<AudienciaResponse> listarAudienciasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim);

    /**
     * Caso de uso: Editar uma audiência existente
     * 
     * @param id ID da audiência
     * @param request Novos dados da audiência
     * @return Audiência atualizada
     * @throws school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException Se a edição criar conflito de horário
     */
    AudienciaResponse editarAudiencia(Long id, CriarAudienciaRequest request);

    /**
     * Caso de uso: Deletar (desativar) uma audiência
     * 
     * @param id ID da audiência
     */
    void deletarAudiencia(Long id);

    /**
     * Caso de uso: Reativar uma audiência desativada
     * 
     * @param id ID da audiência
     */
    void reativarAudiencia(Long id);

    /**
     * Caso de uso: Detectar conflitos de horário para uma audiência
     * 
     * @param request Dados da audiência para verificar conflitos
     * @return Lista de audiências em conflito
     */
    List<AudienciaResponse> detectarConflitos(CriarAudienciaRequest request);
}
