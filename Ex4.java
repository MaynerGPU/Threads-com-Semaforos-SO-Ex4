import java.util.concurrent.*;
import java.util.*;

class CarroF1 extends Thread {
    private String equipe;
    private int idCarro;
    private Semaphore semaforoPista;
    private Semaphore semaforoEquipe;
    private Random random;
    private List<Double> tempos;

    public CarroF1(String equipe, int idCarro, Semaphore semaforoPista, Semaphore semaforoEquipe) {
        this.equipe = equipe;
        this.idCarro = idCarro;
        this.semaforoPista = semaforoPista;
        this.semaforoEquipe = semaforoEquipe;
        this.random = new Random();
        this.tempos = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            semaforoEquipe.acquire(); // Garante que apenas um carro por equipe esteja na pista
            semaforoPista.acquire(); // Garante que no máximo 5 carros estejam na pista
            System.out.println("Carro " + idCarro + " da equipe " + equipe + " entrou na pista.");

            for (int i = 1; i <= 3; i++) {
                double tempoVolta = 50 + (random.nextDouble() * 20); // Tempo entre 50 e 70s
                tempos.add(tempoVolta);
                System.out.println("Carro " + idCarro + " (" + equipe + ") - Volta " + i + ": " + tempoVolta + "s");
                Thread.sleep(1000);
            }

            double melhorTempo = Collections.min(tempos);
            GridLargada.adicionarTempo(equipe, idCarro, melhorTempo);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Carro " + idCarro + " da equipe " + equipe + " saiu da pista.");
            semaforoPista.release();
            semaforoEquipe.release();
        }
    }
}

class GridLargada {
    private static List<String> grid = Collections.synchronizedList(new ArrayList<>());
    
    public static synchronized void adicionarTempo(String equipe, int idCarro, double tempo) {
        grid.add(String.format("Carro %d (%s) - Melhor Volta: %.2fs", idCarro, equipe, tempo));
    }
    
    public static void exibirGrid() {
        System.out.println("\n=== GRID DE LARGADA ===");
        grid.sort(Comparator.comparingDouble(o -> Double.parseDouble(o.split(": ")[1].replace("s", ""))));
        grid.forEach(System.out::println);
    }
}

public class Ex4 {
    public static void main(String[] args) {
        String[] equipes = {"Mercedes", "Ferrari", "Red Bull", "McLaren", "Aston Martin", "Alpine", "Williams"};
        Semaphore semaforoPista = new Semaphore(5); // No máximo 5 carros na pista
        Map<String, Semaphore> semaforosEquipes = new HashMap<>();

        for (String equipe : equipes) {
            semaforosEquipes.put(equipe, new Semaphore(1)); // Um carro por equipe
        }
        
        List<CarroF1> carros = new ArrayList<>();
        int idCarro = 1;
        for (String equipe : equipes) {
            for (int i = 0; i < 2; i++) {
                carros.add(new CarroF1(equipe, idCarro++, semaforoPista, semaforosEquipes.get(equipe)));
            }
        }
        
        for (CarroF1 carro : carros) {
            carro.start();
        }
        
        for (CarroF1 carro : carros) {
            try {
                carro.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        GridLargada.exibirGrid();
    }
}
