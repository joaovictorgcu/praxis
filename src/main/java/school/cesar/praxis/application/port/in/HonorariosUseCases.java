package school.cesar.praxis.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HonorariosUseCases {

    record ItemContrato(Long id,
                        String numeroProcesso,
                        String modalidade,
                        LocalDate celebradoEm,
                        BigDecimal valorContratado,
                        String status,
                        boolean aguardandoResultado,
                        LocalDate proximaParcela) {
    }

    record ItemParcela(int numero,
                       LocalDate vencimento,
                       BigDecimal valor,
                       BigDecimal valorPago,
                       LocalDate dataPagamento,
                       String status,
                       BigDecimal multa,
                       BigDecimal juros,
                       BigDecimal valorAtualizado) {
    }

    record ItemHistorico(String tipo, String descricao, LocalDateTime quando) {
    }

    record DetalheContrato(Long id,
                           String numeroProcesso,
                           String modalidade,
                           LocalDate celebradoEm,
                           BigDecimal valorContratado,
                           String status,
                           boolean aguardandoResultado,
                           int quantidadeParcelas,
                           LocalDate primeiroVencimento,
                           BigDecimal valorFixo,
                           BigDecimal valorHora,
                           int horasTrabalhadas,
                           BigDecimal valorCausaEstimado,
                           BigDecimal percentualExito,
                           BigDecimal valorObtido,
                           BigDecimal honorariosSucumbencia,
                           BigDecimal totalPago,
                           BigDecimal saldoDevedor,
                           List<ItemParcela> parcelas,
                           List<ItemHistorico> historico) {
    }

    interface CadastrarContrato {

        record Comando(String numeroProcesso,
                       String modalidade,
                       LocalDate celebradoEm,
                       BigDecimal valorFixo,
                       BigDecimal valorHora,
                       int horasTrabalhadas,
                       BigDecimal valorCausa,
                       BigDecimal percentualExito,
                       int quantidadeParcelas,
                       LocalDate primeiroVencimento) {
        }

        ItemContrato executar(Comando comando);
    }

    interface ListarContratos {

        /** {@code numeroProcesso} nulo ou em branco lista todos os contratos. */
        List<ItemContrato> executar(String numeroProcesso);
    }

    interface ConsultarContrato {

        ItemContrato executar(Long id);
    }

    interface ConsultarDetalheContrato {

        record Comando(Long contratoId) {
        }

        DetalheContrato executar(Comando comando);
    }

    interface RegistrarPagamentoParcela {

        record Comando(Long contratoId, int numeroParcela, BigDecimal valor, LocalDate dataPagamento) {
        }

        DetalheContrato executar(Comando comando);
    }

    interface RegistrarResultadoCausa {

        record Comando(Long contratoId, BigDecimal valorObtido, BigDecimal honorariosSucumbencia) {
        }

        DetalheContrato executar(Comando comando);
    }
}
