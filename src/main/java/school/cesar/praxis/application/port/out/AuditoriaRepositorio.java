package school.cesar.praxis.application.port.out;

import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;

import java.time.LocalDate;
import java.util.List;

/**
 * Porta de saida: consulta dos eventos de auditoria (quem grava e o
 * {@code RegistradorDeAuditoria}, no dominio - esta porta e so leitura,
 * para a tela de auditoria).
 */
public interface AuditoriaRepositorio {

    List<AcessoUseCases.ItemAuditoria> consultar(String numeroProcesso, String oab,
                                                  ResultadoAcesso resultado, LocalDate de, LocalDate ate);
}
