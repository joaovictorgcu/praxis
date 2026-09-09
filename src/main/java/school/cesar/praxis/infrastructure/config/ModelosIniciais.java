package school.cesar.praxis.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.out.ModeloRepositorio;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.modelo.CodigoModelo;
import school.cesar.praxis.domain.modelo.ModeloDocumento;
import school.cesar.praxis.domain.modelo.TextoModelo;

/**
 * Modelos de exemplo, para o cadastro nascer demonstravel: um que vai a juizo
 * e outro que nao se enderaca ao juizo.
 */
@Component
@ConditionalOnProperty(name = "praxis.modelos-iniciais", havingValue = "true",
        matchIfMissing = true)
public class ModelosIniciais {

    private final ModeloRepositorio modelos;

    public ModelosIniciais(ModeloRepositorio modelos) {
        this.modelos = modelos;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void carregar() {
        if (!modelos.listar().isEmpty()) {
            return;
        }

        modelos.salvar(new ModeloDocumento(
                CodigoModelo.de("COBRANCA_ALUGUEL"),
                "Cobranca de aluguel",
                TipoDocumento.PETICAO_INICIAL,
                null,
                new TextoModelo("""
                        DOS FATOS
                        {{cliente}} celebrou contrato de locacao do imovel situado em \
                        {{enderecoImovel}}, na comarca de {{comarca}}.
                        O locatario deixou de pagar os alugueis, acumulando {{valorDivida}}.

                        DO DIREITO
                        Aplica-se a Lei 8.245/91, que autoriza a cobranca dos alugueis vencidos."""),
                new TextoModelo("""
                        DOS PEDIDOS
                        a) a citacao do reu para pagar ou contestar;
                        b) a condenacao ao pagamento de {{valorDivida}}, com correcao e juros;
                        c) a condenacao em custas e honorarios sucumbenciais."""),
                true));

        modelos.salvar(new ModeloDocumento(
                CodigoModelo.de("ACORDO_EXTRAJUDICIAL"),
                "Acordo extrajudicial",
                TipoDocumento.PECA_AVULSA,
                "Instrumento particular de acordo",
                new TextoModelo("""
                        As partes, sendo {{cliente}} de um lado e {{outraParte}} de outro, \
                        ajustam a composicao amigavel do litigio referente ao processo {{processo}}.

                        O valor ajustado e de {{valorAcordo}}, a ser pago em {{formaPagamento}}."""),
                new TextoModelo("""
                        CLAUSULAS FINAIS
                        a) o cumprimento do acordo extingue a obrigacao discutida;
                        b) as partes dao reciproca quitacao;
                        c) elegem o foro da comarca de {{comarca}} para dirimir duvidas."""),
                false));
    }
}
