package school.cesar.praxis.application.port.in;

import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.ClienteResponse;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.usuario.Usuario;

import java.util.List;

/**
 * Leitura transversal reservada ao chefe: junta em uma consulta so tudo que o
 * escritorio guardou, para a tela de administracao. Nao decide nada e nao
 * altera nada - cada cadastro continua sendo criado e removido pelo seu
 * proprio caso de uso.
 */
public interface AdministracaoUseCases {

    interface ConsultarPanorama {

        /**
         * Retrato do sistema inteiro. As listas vem prontas para a tela; o que
         * nao pode ir para a tela (senha codificada) nao entra aqui.
         */
        record Panorama(List<Usuario> usuarios,
                        List<Processo> processos,
                        List<PrazosUseCases.ConsultarAgenda.ItemAgenda> prazos,
                        List<DocumentosUseCases.ListarDocumentos.ItemDocumento> documentos,
                        List<AnexosUseCases.ItemAnexo> anexos,
                        List<ModelosUseCases.ItemModelo> modelos,
                        List<FeriadosUseCases.ItemFeriado> feriados,
                        List<HonorariosUseCases.ItemContrato> contratos,
                        List<ClienteResponse> clientes,
                        List<ParteContrariaResponse> partesContrarias,
                        List<AudienciaResponse> audiencias) {

            /** Prazos em aberto ja vencidos: o numero que o chefe olha primeiro. */
            public long prazosVencidos() {
                return prazos.stream().filter(PrazosUseCases.ConsultarAgenda.ItemAgenda::vencido).count();
            }

            public long prazosEmAberto() {
                return prazos.stream().filter(item -> !item.cumprido()).count();
            }

            public long documentosEmRevisao() {
                return documentos.stream().filter(d -> "EM_REVISAO".equals(d.status())).count();
            }

            public long processosEmSegredo() {
                return processos.stream().filter(Processo::isSegredoJustica).count();
            }

            public long usuariosComSenhaProvisoria() {
                return usuarios.stream().filter(Usuario::isSenhaProvisoria).count();
            }

            /** Soma dos andamentos de todos os processos: o tamanho do historico. */
            public long andamentos() {
                return processos.stream().mapToInt(Processo::quantidadeAndamentos).sum();
            }
        }

        Panorama executar();
    }
}
