package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.domain.prazo.NivelAlerta;
import school.cesar.praxis.domain.prazo.OrigemVarredura;
import school.cesar.praxis.domain.prazo.PeriodoSuspensao;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.PrerrogativaPrazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Portas de entrada da funcionalidade <b>Motor de prazos processuais com alertas</b>.
 * Agrupadas em um arquivo por serem contratos pequenos do mesmo caso de negocio.
 */
public interface PrazosUseCases {

    /**
     * Quem age sobre o prazo. Vai para a trilha ({@code prazo_evento}) e decide
     * o que e permitido: cancelar e reabrir sao do responsavel ou do chefe.
     */
    record Autor(String oab, String nome, boolean chefe) {

        /** Acao sem usuario logado (carga de exemplo, agendador). */
        public static Autor sistema() {
            return new Autor(null, "sistema", false);
        }

        public static Autor ouSistema(Autor autor) {
            return autor == null ? sistema() : autor;
        }
    }

    interface AbrirPrazo {

        record Comando(String numeroProcesso,
                       String descricao,
                       LocalDate intimacao,
                       int quantidadeDias,
                       boolean fatal,
                       RegimeContagem regime,
                       PrerrogativaPrazo prerrogativa,
                       Autor autor) {

            /** Prazo simples aberto sem usuario identificado (carga de exemplo, testes). */
            public Comando(String numeroProcesso,
                           String descricao,
                           LocalDate intimacao,
                           int quantidadeDias,
                           boolean fatal,
                           RegimeContagem regime) {
                this(numeroProcesso, descricao, intimacao, quantidadeDias, fatal, regime,
                        PrerrogativaPrazo.NENHUMA, null);
            }
        }

        Prazo executar(Comando comando);
    }

    interface CumprirPrazo {

        default Prazo executar(Long prazoId) {
            return executar(prazoId, null);
        }

        Prazo executar(Long prazoId, Autor autor);
    }

    /** CPC art. 221: o prazo para de correr a partir de {@code inicio}. */
    interface SuspenderPrazo {

        record Comando(Long prazoId, LocalDate inicio, String motivo, Autor autor) {
        }

        Prazo executar(Comando comando);
    }

    /** O prazo volta a correr em {@code retomada} e e recontado com o saldo que faltava. */
    interface RetomarPrazo {

        record Comando(Long prazoId, LocalDate retomada, Autor autor) {
        }

        Prazo executar(Comando comando);
    }

    /** CPC art. 139, VI: o juiz dilata o prazo, so antes de encerrado o prazo regular. */
    interface DilatarPrazo {

        record Comando(Long prazoId, int dias, String fundamento, Autor autor) {
        }

        Prazo executar(Comando comando);
    }

    interface CancelarPrazo {

        record Comando(Long prazoId, String motivo, Autor autor) {
        }

        Prazo executar(Comando comando);
    }

    interface ReabrirPrazo {

        record Comando(Long prazoId, String motivo, Autor autor) {
        }

        Prazo executar(Comando comando);
    }

    /** Ficha completa do prazo: contagem, suspensoes e a trilha de eventos gravada. */
    interface ConsultarPrazo {

        record DetalhePrazo(Long id,
                            String numeroProcesso,
                            String descricao,
                            LocalDate intimacao,
                            int quantidadeDias,
                            RegimeContagem regime,
                            PrerrogativaPrazo prerrogativa,
                            int diasAcrescidos,
                            int diasEfetivos,
                            LocalDate vencimentoOriginal,
                            LocalDate vencimento,
                            int diasRestantes,
                            boolean vencido,
                            boolean fatal,
                            SituacaoPrazo situacao,
                            LocalDate cumpridoEm,
                            String responsavelNome,
                            String responsavelOab,
                            Set<NivelAlerta> alertasEmitidos,
                            List<PeriodoSuspensao> suspensoes,
                            List<EventoPrazo> eventos) {

            public boolean vencimentoAlterado() {
                return !vencimento.equals(vencimentoOriginal);
            }
        }

        DetalhePrazo executar(Long prazoId);
    }

    /** Varredura da agenda: avalia prazos em aberto, dispara alertas e grava a execucao. */
    interface VarrerPrazos {

        record Comando(LocalDate dataReferencia, OrigemVarredura origem, String solicitante) {
        }

        record Resultado(ExecucaoVarredura execucao, List<AlertaPrazo> alertas) {
        }

        Resultado executar(Comando comando);

        /** Varredura manual numa data informada (testes, "Simular Data"). */
        default List<AlertaPrazo> executar(LocalDate hoje) {
            return executar(new Comando(hoje, OrigemVarredura.MANUAL, null)).alertas();
        }

        /** Caminho do agendador diario: data do relogio, origem automatica. */
        List<AlertaPrazo> executarHoje();
    }

    /** Historico das execucoes da varredura, da mais recente para a mais antiga. */
    interface ListarVarreduras {

        List<ExecucaoVarredura> executar(int limite);
    }

    interface ConsultarAgenda {

        record ItemAgenda(Long prazoId,
                          String numeroProcesso,
                          String descricao,
                          LocalDate vencimento,
                          int diasRestantes,
                          boolean fatal,
                          boolean vencido,
                          String responsavel,
                          boolean cumprido,
                          String responsavelOab,
                          SituacaoPrazo situacao) {
        }

        List<ItemAgenda> executar(LocalDate ate);
    }

    /**
     * Agenda inteira, inclusive o que ja foi cumprido. A tela do dia mostra so
     * o que ainda exige acao; o panorama do chefe precisa do historico completo.
     */
    interface ListarTodosOsPrazos {

        List<ConsultarAgenda.ItemAgenda> executar();
    }

    /** Todos os prazos (abertos e cumpridos) de um processo, para a tela do processo. */
    interface ConsultarPrazosDoProcesso {

        List<ConsultarAgenda.ItemAgenda> executar(String numeroProcesso);
    }
}
