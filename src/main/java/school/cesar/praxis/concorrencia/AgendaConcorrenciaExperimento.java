package school.cesar.praxis.concorrencia;

import school.cesar.praxis.domain.agenda.AgendaDeAudiencias;
import school.cesar.praxis.domain.agenda.Audiencia;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AgendaConcorrenciaExperimento {

    private final Object lock = new Object();

    public ResultadoRodada executarBaseline(int numeroThreads, int rodadas, String sala) {
        int aceitas = 0;
        int rejeitadas = 0;
        boolean violacao = false;

        for (int i = 0; i < rodadas; i++) {
            ResultadoRodada rodada = executarUmRound(numeroThreads, sala, false);
            aceitas += rodada.getAceitas();
            rejeitadas += rodada.getRejeitadas();
            violacao = violacao || rodada.houveViolacao();
        }

        return new ResultadoRodada(numeroThreads, rodadas * numeroThreads, aceitas, rejeitadas, violacao);
    }

    public ResultadoRodada executarSincronizada(int numeroThreads, int rodadas, String sala) {
        int aceitas = 0;
        int rejeitadas = 0;
        boolean violacao = false;

        for (int i = 0; i < rodadas; i++) {
            ResultadoRodada rodada = executarUmRound(numeroThreads, sala, true);
            aceitas += rodada.getAceitas();
            rejeitadas += rodada.getRejeitadas();
            violacao = violacao || rodada.houveViolacao();
        }

        return new ResultadoRodada(numeroThreads, rodadas * numeroThreads, aceitas, rejeitadas, violacao);
    }

    public ResultadoRodada executarBaseline(int numeroThreads, String sala) {
        return executarUmRound(numeroThreads, sala, false);
    }

    public ResultadoRodada executarSincronizada(int numeroThreads, String sala) {
        return executarUmRound(numeroThreads, sala, true);
    }

    public List<ResultadoRodada> executarCincoRodadas(int numeroThreads, String sala) {
        List<ResultadoRodada> rodadas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rodadas.add(executarSincronizada(numeroThreads, sala));
        }
        return rodadas;
    }

    private ResultadoRodada executarUmRound(int numeroThreads, String sala, boolean sincronizado) {
        List<Audiencia> audiencias = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(numeroThreads);
        AtomicInteger aceitas = new AtomicInteger();
        AtomicInteger rejeitadas = new AtomicInteger();

        for (int i = 0; i < numeroThreads; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    start.await();
                    Audiencia tentativa = novaAudiencia(index, sala);
                    if (sincronizado) {
                        registrarSincronizado(audiencias, tentativa);
                    } else {
                        registrarSemSincronizacao(audiencias, tentativa);
                    }
                    aceitas.incrementAndGet();
                } catch (ConflitoDEAudienciaException e) {
                    rejeitadas.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    rejeitadas.incrementAndGet();
                }
            });
        }

        start.countDown();
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
        }

        int totalAceitas = aceitas.get();
        int totalRejeitadas = rejeitadas.get();
        boolean violacao = totalAceitas > 1;
        return new ResultadoRodada(numeroThreads, numeroThreads, totalAceitas, totalRejeitadas, violacao);
    }

    private void registrarSemSincronizacao(List<Audiencia> audiencias, Audiencia tentativa) {
        if (AgendaDeAudiencias.de(audiencias).temConflito(tentativa)) {
            throw new ConflitoDEAudienciaException("Conflito detectado na tentativa concorrente");
        }

        try {
            Thread.sleep(15L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        audiencias.add(tentativa);
    }

    private void registrarSincronizado(List<Audiencia> audiencias, Audiencia tentativa) {
        synchronized (lock) {
            if (AgendaDeAudiencias.de(audiencias).temConflito(tentativa)) {
                throw new ConflitoDEAudienciaException("Conflito detectado na tentativa concorrente");
            }
            audiencias.add(tentativa);
        }
    }

    private Audiencia novaAudiencia(int numero, String sala) {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 21, 10, 0);
        return new Audiencia(
                "Proc. 9999/2026-" + numero,
                "Parte Autora " + numero,
                inicio,
                inicio.plusHours(1),
                sala);
    }

    public static class ResultadoRodada {
        private final int numeroThreads;
        private final int tentativas;
        private final int aceitas;
        private final int rejeitadas;
        private final boolean violacao;

        public ResultadoRodada(int numeroThreads, int tentativas, int aceitas, int rejeitadas, boolean violacao) {
            this.numeroThreads = numeroThreads;
            this.tentativas = tentativas;
            this.aceitas = aceitas;
            this.rejeitadas = rejeitadas;
            this.violacao = violacao;
        }

        public int getNumeroThreads() {
            return numeroThreads;
        }

        public int getTentativas() {
            return tentativas;
        }

        public int getAceitas() {
            return aceitas;
        }

        public int getRejeitadas() {
            return rejeitadas;
        }

        public boolean houveViolacao() {
            return violacao;
        }

        @Override
        public String toString() {
            return "ResultadoRodada{" +
                    "numeroThreads=" + numeroThreads +
                    ", tentativas=" + tentativas +
                    ", aceitas=" + aceitas +
                    ", rejeitadas=" + rejeitadas +
                    ", violacao=" + violacao +
                    '}';
        }
    }
}
