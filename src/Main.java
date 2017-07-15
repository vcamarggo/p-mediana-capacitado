import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Vinícius Camargo
 *
 * @date 10/07/2017
 * 
 * @opt 17.288,99
 */
class Main {
    private static class Vertice {
	private int x;
	private int y;
	private short capacidade;
	private short demanda;

	public int getX() {
	    return x;
	}

	public int getY() {
	    return y;
	}

	public Vertice(int x, int y, short capacidade, short demanda) {
	    super();
	    this.x = x;
	    this.y = y;
	    this.capacidade = capacidade;
	    this.demanda = demanda;
	}
    }

    private static class Mediana extends Vertice {
	private List<Vertice> vertices;

	public Mediana(int x, int y, short capacidade, short demanda) {
	    super(x, y, capacidade, demanda);
	}

	public List<Vertice> getVertices() {
	    return vertices;
	}

	public double findDistance(Vertice c2) {
	    int diferencaX = this.getX() - c2.getX();
	    int diferencaY = this.getY() - c2.getY();
	    double dist = Math.pow(diferencaX, 2) + Math.pow(diferencaY, 2);
	    double distance = Math.sqrt(dist);
	    return distance;
	}
    }

    private static class Solucao {
	private double qualidade;
	private List<Mediana> medianas = new ArrayList<>();

	public Solucao() {
	    // algoritmo de resolver

	}

	public void mutate() {
	    Random rand = new Random();
	    int index = rand.nextInt(nrMedianas);
	    this.setGene(index, 1 - this.getGene(index)); // flip
	}

	public void evaluate() {
	    int fitness = 0;
	    for (Mediana mediana : medianas) {
		for (Vertice vertice : mediana.getVertices()) {
		    fitness += mediana.findDistance(vertice);
		}
	    }
	    qualidade = fitness;
	}

	public List<Mediana> getMedianas() {
	    return medianas;
	}

	public double getQualidade() {
	    return qualidade;
	}

    }

    private static class Populacao {
	private static Solucao[] solucoes;
	private double totalFitness;

	public Populacao() {
	    solucoes = new Solucao[TAM_POP];

	    // init population
	    for (int i = 0; i < TAM_POP; i++) {
		solucoes[i] = new Solucao();
		solucoes[i].evaluate();
	    }

	    this.evaluateTotal();
	}

	public Solucao[] crossover(Solucao velha1, Solucao velha2) {
	    Solucao[] newIndiv = new Solucao[2];
	    newIndiv[0] = new Solucao();
	    newIndiv[1] = new Solucao();

	    int randPoint = randomizer.nextInt(nrMedianas);
	    int i;
	    for (i = 0; i < randPoint; ++i) {
		if (!velha1.getMedianas().contains(velha2.getMedianas().get(i))
			&& !velha2.getMedianas().contains(velha1.getMedianas().get(i))) {
		    newIndiv[0].getMedianas().add(velha1.getMedianas().get(i));
		    newIndiv[1].getMedianas().add(velha2.getMedianas().get(i));
		}
	    }

	    newIndiv[0].evaluate();
	    newIndiv[1].evaluate();

	    return newIndiv;
	}

	public Solucao findBestSolucao() {
	    int idxMin = 0;
	    double currentMin = 1.0;
	    double currentVal;

	    for (int idx = 0; idx < 100; ++idx) {
		currentVal = solucoes[idx].getQualidade();
		if (currentVal < currentMin) {
		    currentMin = currentVal;
		    idxMin = idx;
		}
	    }

	    return solucoes[idxMin]; // minimization
	}

	public double evaluateTotal() {
	    this.totalFitness = 0.0;
	    for (int i = 0; i < 100; i++) {
		this.totalFitness += solucoes[i].getQualidade();
	    }
	    return this.totalFitness;
	}

	public Solucao rouletteWheelSelection() {
	    double randNum = randomizer.nextDouble() * this.totalFitness;
	    int idx;
	    for (idx = 0; idx < TAM_POP && randNum > 0; ++idx) {
		randNum -= solucoes[idx].getQualidade();
	    }
	    return solucoes[idx - 1];
	}

	public double getTotalFitness() {
	    return totalFitness;
	}

	public void setPopulation(Solucao[] newPop) {
	    // this.m_population = newPop;
	    System.arraycopy(newPop, 0, solucoes, 0, TAM_POP);
	}
    }

    private static final short TEMPO_MAX_MILISSEGUNDOS = 8000;
    private static final short ELITISMO = 6;
    private static final float TAXA_MUTACAO = 0.1f;
    private static final float TAXA_CRUZAMENTO = 0.7f;
    private static final short TAM_POP = 100;
    private static final short MAX_ITER = 2000;

    private static int nrVertices;
    private static int nrMedianas;
    private static Vertice[] medianas;
    static Random randomizer = new Random();

    public static void main(String[] args) throws FileNotFoundException {
	Scanner scan = new Scanner(new FileReader(Main.class.getResource("1.in").getPath()));
	nrVertices = scan.nextInt();
	nrMedianas = scan.nextShort();
	medianas = new Vertice[nrVertices];
	for (int i = 0; i < nrVertices; i++) {
	    medianas[i] = new Vertice(scan.nextInt(), scan.nextInt(), scan.nextShort(), scan.nextShort());
	}
	scan.close();

	pMedianaCapacitada();
    }

    private static void pMedianaCapacitada() {
	Populacao pop = new Populacao();
	Solucao[] newPop = new Solucao[TAM_POP];
	Solucao[] indiv = new Solucao[2];

	long tempoFinal = System.currentTimeMillis() + TEMPO_MAX_MILISSEGUNDOS;
	int count;
	for (int iter = 0; iter < MAX_ITER && tempoFinal > System.currentTimeMillis(); iter++) {
	    count = 0;

	    // Elitism
	    for (int x = 0; x < ELITISMO; ++x) {
		newPop[count] = pop.findBestSolucao();
		count++;
	    }
	    while (count < TAM_POP) {
		Solucao pai = pop.rouletteWheelSelection();
		Solucao mae = pop.rouletteWheelSelection();
		if (randomizer.nextDouble() < TAXA_CRUZAMENTO) {
		    indiv = pop.crossover(mae, pai);
		}

		// Mutation
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[0].mutate();
		}
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[1].mutate();
		}

		// add to new population
		newPop[count] = indiv[0];
		newPop[count + 1] = indiv[1];
		count += 2;
	    }
	    pop.setPopulation(newPop);

	    // reevaluate current population
	    pop.evaluateTotal();
	    System.out.print("Maior qualidade = " + pop.getTotalFitness());
	    System.out.println(" Maior qualidade = " + pop.findBestSolucao().getQualidade());
	}

	Solucao bestIndiv = pop.findBestSolucao(); // {//
	System.out.println(bestIndiv);
    }

}
