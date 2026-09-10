package school.cesar.praxis.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HonorariosUseCases {

    record ItemContrato(Long id,
                        String numeroProcesso,
                        String modalidade,
                        LocalDate celebradoEm,
                        BigDecimal valorContratado) {
    }

    interface CadastrarContrato {

        record Comando(String numeroProcesso,
                       String modalidade,
                       LocalDate celebradoEm,
                       BigDecimal valorFixo,
                       BigDecimal valorHora,
                       int horasTrabalhadas,
                       BigDecimal valorCausa,
                       BigDecimal percentualExito) {
        }

        ItemContrato executar(Comando comando);
    }

    interface ListarContratos {

        List<ItemContrato> executar(String numeroProcesso);
    }

    interface ConsultarContrato {

        ItemContrato executar(Long id);
    }
}
