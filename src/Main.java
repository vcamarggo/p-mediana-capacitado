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
	protected int x;
	protected int y;
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

	public short getCapacidade() {
	    return capacidade;
	}

	public short getDemanda() {
	    return demanda;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + x;
	    result = prime * result + y;
	    return result;
	}

	public Mediana encontraMedianaMaisProximaComCapacidade(List<Mediana> medianas) {
	    int idxMin = -1;
	    double currentMin = Double.MAX_VALUE;
	    double currentVal;
	    for (int iter = 0; iter < medianas.size(); iter++) {
		currentVal = medianas.get(iter).findDistance(this);
		if (currentVal < currentMin && medianas.get(iter).temCapacidade(demanda)) {
		    currentMin = currentVal;
		    idxMin = iter;
		}
	    }
	    if (idxMin == -1) {
		return null;
	    }
	    return medianas.get(idxMin);
	}

    }

    private static class Mediana extends Vertice {
	private int somaDasDistancias;
	private short capacidadeUsada;

	public Mediana(int x, int y, short capacidade, short demanda) {
	    super(x, y, capacidade, demanda);
	}

	public Mediana(Vertice v) {
	    super(v.getX(), v.getY(), v.getCapacidade(), v.getDemanda());
	}

	public double findDistance(Vertice c2) {
	    int diferencaX = this.getX() - c2.getX();
	    int diferencaY = this.getY() - c2.getY();
	    double dist = Math.pow(diferencaX, 2) + Math.pow(diferencaY, 2);
	    double distance = Math.sqrt(dist);
	    return distance;
	}

	public int getSomaDasDistancias() {
	    return somaDasDistancias;
	}

	public void acrescentaDistancia(double distancia) {
	    somaDasDistancias += distancia;
	}

	public void diminuiCapacidade(short demanda) {
	    capacidadeUsada += demanda;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    Vertice other = (Vertice) obj;
	    if (x != other.x)
		return false;
	    if (y != other.y)
		return false;
	    return true;
	}

	public boolean temCapacidade(short demanda) {
	    return capacidadeUsada + demanda < getCapacidade();
	}

	// Esta funcao assume que o teste de capacidade ja foi realizado
	public void adicionaDadosVertices(Vertice vertice) {
	    acrescentaDistancia(findDistance(vertice));
	    diminuiCapacidade(vertice.getDemanda());
	}

    }

    private static class Solucao {
	private double qualidade;
	private List<Mediana> medianas = new ArrayList<>();

	public Solucao() {
	}

	public void mutate() {
	    Random rand = new Random();
	    int index = rand.nextInt(nrMedianas);
	    this.getMedianas().remove(index);
	    Vertice v = null;
	    boolean trocou = false;
	    do {
		index = rand.nextInt(nrVertices);
		v = vertices[index];
		if (!this.getMedianas().contains(v)) {
		    this.getMedianas().add(new Mediana(v));
		    trocou = true;
		}
	    } while (!trocou);
	}

	public void evaluate(int indicesPercorridos) {
	    if (nrVertices - indicesPercorridos > 0) {
		penaliza(PENALIZACAO + (1 - (indicesPercorridos / nrVertices)));
	    }
	    int fitness = 0;
	    for (Mediana mediana : medianas) {
		fitness += mediana.getSomaDasDistancias();
	    }
	    qualidade = fitness;
	    System.out.println(" Qualidade LOCAL = " + qualidade);
	}

	public List<Mediana> getMedianas() {
	    return medianas;
	}

	public double getQualidade() {
	    return qualidade;
	}

	public int geraRand() {
	    criaPrimeirasMedianas();
	    return ligaVerticesAsMedianasERetornaUltimoIndice();
	}

	private int ligaVerticesAsMedianasERetornaUltimoIndice() {
	    int nrIndicesAdicionados = 0;

	    for (int j = 0; j < nrVertices; j++) {
		if (!(vertices[j] instanceof Mediana)) {
		    // TODO testar se esta mediana será alterada
		    Mediana mediana = vertices[j].encontraMedianaMaisProximaComCapacidade(getMedianas());
		    if (mediana != null) {
			mediana.adicionaDadosVertices(vertices[j]);
			nrIndicesAdicionados++;
		    }
		}
	    }
	    return nrIndicesAdicionados;
	}

	private void criaPrimeirasMedianas() {
	    short nrMed = 0;
	    while (nrMed < nrMedianas) {
		int indice = randomizer.nextInt(nrVertices);
		Vertice v = vertices[indice];
		if (!getMedianas().contains(v)) {
		    adicionaMediana(v);
		    nrMed++;
		}
	    }
	}

	private void adicionaMediana(Vertice v) {
	    this.medianas.add(new Mediana(v));

	}

	public void penaliza(double porcentagemPenalizacao) {
	    qualidade = qualidade * porcentagemPenalizacao;
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

		int indicesPercorridos = solucoes[i].geraRand();
		solucoes[i].evaluate(indicesPercorridos);
	    }
	    this.evaluateTotal();
	}

	public Solucao[] crossover(Solucao velha0, Solucao velha1) {
	    Solucao[] newIndiv = new Solucao[2];
	    newIndiv[0] = new Solucao();
	    newIndiv[1] = new Solucao();

	    if (velha0.getMedianas().size() != 10)
		System.out.println("merda");
	    if (velha1.getMedianas().size() != 10)
		System.out.println("merda");

	    int randPoint = randomizer.nextInt(nrMedianas);
	    int i;
	    for (i = 0; i < nrMedianas; i++) {
		if (i < randPoint && !velha1.getMedianas().contains(velha0.getMedianas().get(i))
			&& !velha0.getMedianas().contains(velha1.getMedianas().get(i))) {
		    newIndiv[0].getMedianas().add(velha1.getMedianas().get(i));

		    newIndiv[1].getMedianas().add(velha0.getMedianas().get(i));
		} else {
		    newIndiv[0].getMedianas().add(velha0.getMedianas().get(i));
		    newIndiv[1].getMedianas().add(velha1.getMedianas().get(i));
		}
	    }

	    int indicesPercorridos0 = newIndiv[0].ligaVerticesAsMedianasERetornaUltimoIndice();
	    newIndiv[0].evaluate(indicesPercorridos0);

	    int indicesPercorridos1 = newIndiv[1].ligaVerticesAsMedianasERetornaUltimoIndice();
	    newIndiv[1].evaluate(indicesPercorridos1);

	    return newIndiv;
	}

	public Solucao findBestSolucao() {
	    int idxMin = 0;
	    double currentMin = 1.0;
	    double currentVal;

	    for (int idx = 0; idx < TAM_POP; ++idx) {
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
	    for (int i = 0; i < TAM_POP; i++) {
		this.totalFitness += solucoes[i].getQualidade();
	    }
	    return this.totalFitness;
	}

	public Solucao rouletteWheelSelection() {
	    // double randNum = randomizer.nextDouble() * this.totalFitness;
	    int idx = randomizer.nextInt(TAM_POP);
	    // for (idx = 0; idx < TAM_POP && randNum > 0; ++idx) {
	    // randNum -= solucoes[idx].getQualidade();
	    // }
	    return solucoes[idx];
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
    private static final short ELITISMO = 0;
    private static final float TAXA_MUTACAO = 0.0f;
    private static final short TAM_POP = 20;
    private static final short MAX_ITER = 20;
    private static final short PENALIZACAO = 10;

    private static int nrVertices;
    private static int nrMedianas;
    private static Vertice[] vertices;
    static Random randomizer = new Random();

    public static void main(String[] args) throws FileNotFoundException {
	Scanner scan = new Scanner(new FileReader(Main.class.getResource("1.in").getPath()));
	nrVertices = scan.nextInt();
	nrMedianas = scan.nextShort();
	vertices = new Vertice[nrVertices];
	for (int i = 0; i < nrVertices; i++) {
	    vertices[i] = new Vertice(scan.nextInt(), scan.nextInt(), scan.nextShort(), scan.nextShort());
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
	// for (int iter = 0; iter < MAX_ITER && tempoFinal >
	// System.currentTimeMillis(); iter++) {
	for (int iter = 0; iter < MAX_ITER; iter++) {
	    count = 0;

	    // Elitism
	    for (int x = 0; x < ELITISMO; ++x) {
		// CORRIGIR
		newPop[count] = pop.findBestSolucao();
		count++;
	    }
	    while (count < TAM_POP) {
		Solucao pai = pop.rouletteWheelSelection();
		Solucao mae = pop.rouletteWheelSelection();
		indiv = pop.crossover(mae, pai);

		// Mutation
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[0].mutate();
		    int indicesPercorridos = indiv[0].ligaVerticesAsMedianasERetornaUltimoIndice();
		    indiv[0].evaluate(indicesPercorridos);
		}
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[1].mutate();
		    int indicesPercorridos = indiv[1].ligaVerticesAsMedianasERetornaUltimoIndice();
		    indiv[1].evaluate(indicesPercorridos);
		}

		// add to new population
		newPop[count] = indiv[0];
		newPop[count + 1] = indiv[1];
		count += 2;
	    }
	    pop.setPopulation(newPop);

	    // reevaluate current population
	    pop.evaluateTotal();
	    System.out.print("Qualidade total = " + pop.getTotalFitness());
	    System.out.println(" Máxima qualidade = " + pop.findBestSolucao().getQualidade());
	}

	Solucao bestIndiv = pop.findBestSolucao(); // {//
	System.out.println(bestIndiv.getQualidade());
    }

}
