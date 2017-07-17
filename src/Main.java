import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Vinícius Camargo , Fernando Carvalho
 *
 * @date 10/07/2017
 * 
 * @opt 17.288,99
 */
class Main {
    // PARAMETROS
    private static final short TEMPO_MAX_MILISSEGUNDOS = 9300;
    private static final short FACILIDADE_PAI_BOM = 70;
    private static final short FACILIDADE_MAE_BOM = 100;
    private static final short ELITISMO = 500;
    private static final float TAXA_MUTACAO = 0.05f;
    // private static final short ITER_SEM_MOD = 50;
    private static final short TAM_POP = 7000;
    private static final short PENALIZACAO = Short.MAX_VALUE;
    private static final short ALEATORIEDADE_ROLETA = 1000;

    /**
     * Classe que representa o vertice que sera carregado e mantido inalterado
     *
     */
    private static class Vertice {
	protected int x;
	protected int y;
	private short capacidade;
	private short demanda;

	public Vertice(int x, int y, short capacidade, short demanda) {
	    super();
	    this.x = x;
	    this.y = y;
	    this.capacidade = capacidade;
	    this.demanda = demanda;
	}

	public int getX() {
	    return x;
	}

	public int getY() {
	    return y;
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

	/**
	 * 
	 * @param medianas
	 * @return menor mediana que contem capacidade livre
	 */
	public Mediana encontraMedianaMaisProximaComCapacidade(List<Mediana> medianas) {
	    int idxMin = -1;
	    double currentMin = Double.MAX_VALUE;
	    double currentVal;
	    for (int iter = 0; iter < medianas.size(); iter++) {
		currentVal = medianas.get(iter).encontraDistancia(this); // calcula
		// distancia
		// entre o
		// vertice e
		// a mediana
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

    }

    /**
     * 
     * Classe que e um vertice que representa uma mediana
     */
    private static class Mediana extends Vertice {
	private double somaDasDistancias;
	private short capacidadeUsada;

	public Mediana(Vertice v) {
	    super(v.getX(), v.getY(), v.getCapacidade(), v.getDemanda());
	    capacidadeUsada = v.getDemanda();
	}

	/**
	 * @param vertice
	 * @return distancia entre a mediana e o vertice
	 */
	public double encontraDistancia(Vertice vertice) {
	    int diferencaX = vertice.getX() - this.getX();
	    int diferencaY = vertice.getY() - this.getY();
	    double dist = Math.pow(diferencaX, 2) + Math.pow(diferencaY, 2);
	    double distance = Math.sqrt(dist);
	    return distance;
	}

	public double getSomaDasDistancias() {
	    return somaDasDistancias;
	}

	public void acrescentaDistancia(double distancia) {
	    somaDasDistancias += distancia;
	}

	public void diminuiCapacidade(short demanda) {
	    capacidadeUsada += demanda;
	}

	public boolean temCapacidade(short demanda) {
	    return capacidadeUsada + demanda <= getCapacidade();
	}

	// Esta funcao assume que o teste de capacidade ja foi realizado
	public void adicionaDadosVertices(Vertice vertice) {
	    acrescentaDistancia(encontraDistancia(vertice));
	    diminuiCapacidade(vertice.getDemanda());
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Mediana other = (Mediana) obj;
	    if (capacidadeUsada != other.capacidadeUsada)
		return false;
	    if (Double.doubleToLongBits(somaDasDistancias) != Double.doubleToLongBits(other.somaDasDistancias))
		return false;
	    return true;
	}

    }

    /**
     * Esta classe representa uma instancia da solucao
     *
     */
    private static class Solucao implements Comparable<Solucao> {
	private double qualidade;
	private List<Mediana> medianas = new ArrayList<>();

	public List<Mediana> getMedianas() {
	    return medianas;
	}

	public double getQualidade() {
	    return qualidade;
	}

	/**
	 * operador de mutacao
	 */
	public void fazMutacao() {
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

	public void avaliarQualidadeSolucao(int indicesPercorridos) {
	    int fitness = 0;
	    for (Mediana mediana : medianas) {
		fitness += mediana.getSomaDasDistancias();
	    }
	    qualidade = fitness;
	    if (nrVertices - (indicesPercorridos + medianas.size()) > 0) {
		penaliza(PENALIZACAO + (1 - (indicesPercorridos / nrVertices)));
	    }
	}

	public int geraSolucaoAleatoria() {
	    criaPrimeirasMedianas();
	    return ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	}

	private int ligaVerticesAsMedianasERetornaUltimoIndiceUsado() {
	    int nrIndicesAdicionados = 0;

	    for (int j = 0; j < nrVertices; j++) {
		if (!(medianas.contains(vertices[j]))) {
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

	@Override
	public int compareTo(Solucao o) {
	    return (qualidade > o.getQualidade()) ? 1 : (qualidade < o.getQualidade()) ? -1 : 0;
	}

    }

    /**
     * Esta classe representa o conjunto de todas solucoes
     *
     */
    private static class Populacao {
	private Solucao[] solucoes;
	private double totalFitness;

	public Populacao() {
	    solucoes = new Solucao[TAM_POP];

	    for (int i = 0; i < TAM_POP; i++) {
		solucoes[i] = new Solucao();
		int indicesPercorridos = solucoes[i].geraSolucaoAleatoria();
		solucoes[i].avaliarQualidadeSolucao(indicesPercorridos);
	    }

	    this.avaliarQualidadePopulacao();
	}

	public void setPopulation(Solucao[] newPop) {
	    System.arraycopy(newPop, 0, solucoes, 0, TAM_POP);
	}

	public Solucao[] getSolucoes() {
	    return solucoes;
	}

	/**
	 * Operador de reprodumecao
	 * 
	 * @param velha0
	 * @param velha1
	 * @return duas solucoes baseadas nos parametros
	 */
	public Solucao[] fazCruzamento(Solucao velha0, Solucao velha1) {
	    Solucao[] newIndiv = new Solucao[2];
	    newIndiv[0] = new Solucao();
	    newIndiv[1] = new Solucao();

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

	    int indicesPercorridos0 = newIndiv[0].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	    newIndiv[0].avaliarQualidadeSolucao(indicesPercorridos0);

	    int indicesPercorridos1 = newIndiv[1].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	    newIndiv[1].avaliarQualidadeSolucao(indicesPercorridos1);

	    return newIndiv;
	}

	// A melhor solucao e a que tem menor qualidade (soma das distancia)
	public Solucao encontraMelhorSolucaoDaLista() {
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
	    return solucoes[idxMin];
	}

	public double avaliarQualidadePopulacao() {
	    this.totalFitness = 0.0;
	    for (int i = 0; i < TAM_POP; i++) {
		this.totalFitness += solucoes[i].getQualidade();
	    }
	    return this.totalFitness;
	}

	/**
	 * Operador de selecao
	 */
	public Solucao selecaoDeRoleta(int facilidadeDosMelhores) {
	    double randNum = this.totalFitness / randomizer.nextInt(ALEATORIEDADE_ROLETA);
	    int idx;
	    for (idx = 0; idx < TAM_POP - 1 && randNum > 0; ++idx) {
		randNum -= facilidadeDosMelhores * solucoes[idx].getQualidade();
	    }
	    return solucoes[idx];
	}

    }

    private static int nrVertices;
    private static int nrMedianas;
    private static Vertice[] vertices;
    static Random randomizer = new Random();

    public static void main(String[] args) throws FileNotFoundException {
	long start = System.currentTimeMillis();

	Scanner scan = new Scanner(new FileReader(Main.class.getResource("1.in").getPath()));
	nrVertices = scan.nextInt();
	nrMedianas = scan.nextShort();
	vertices = new Vertice[nrVertices];
	for (int i = 0; i < nrVertices; i++) {
	    vertices[i] = new Vertice(scan.nextInt(), scan.nextInt(), scan.nextShort(), scan.nextShort());
	}
	scan.close();

	System.out.println("Soma das distancias: " + pMedianaCapacitada());
	System.out.println("Millisegundos: " + (System.currentTimeMillis() - start));
	System.out.println("Memoria Usada: "
		+ new DecimalFormat("#.##").format(
			((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576))
		+ "Mb");
    }

    private static double pMedianaCapacitada() {
	Populacao pop = new Populacao();
	Solucao[] newPop = new Solucao[TAM_POP];
	Solucao[] indiv = new Solucao[2];

	long tempoFinal = System.currentTimeMillis() + TEMPO_MAX_MILISSEGUNDOS;
	int count;

	// double melhor = Double.MAX_VALUE;
	// for (int nrSemMod = 0; nrSemMod < ITER_SEM_MOD;) {
	for (; tempoFinal > System.currentTimeMillis();) {
	    count = 0;
	    Arrays.sort(pop.getSolucoes());

	    /*
	     * if (melhor <= pop.getSolucoes()[0].getQualidade()) { nrSemMod++;
	     * } else { melhor = pop.getSolucoes()[0].getQualidade(); nrSemMod =
	     * 0; System.out.println(melhor); }
	     */

	    // Conceito de Elitismo
	    for (int x = 0; x < ELITISMO; ++x) {
		newPop[count] = pop.getSolucoes()[count];
		count++;
	    }
	    while (count < TAM_POP) {
		Solucao pai = pop.selecaoDeRoleta(FACILIDADE_PAI_BOM);
		Solucao mae = pop.selecaoDeRoleta(FACILIDADE_MAE_BOM);
		indiv = pop.fazCruzamento(mae, pai);

		// Conceito de Mutacao
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[0].fazMutacao();
		    int indicesPercorridos = indiv[0].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
		    indiv[0].avaliarQualidadeSolucao(indicesPercorridos);
		}
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[1].fazMutacao();
		    int indicesPercorridos = indiv[1].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
		    indiv[1].avaliarQualidadeSolucao(indicesPercorridos);
		}

		newPop[count] = indiv[0];
		newPop[count + 1] = indiv[1];
		count += 2;
	    }
	    // Conceito de Nova Geracao Completa
	    pop.setPopulation(newPop);
	    pop.avaliarQualidadePopulacao();
	}

	return pop.encontraMelhorSolucaoDaLista().getQualidade();
    }

}
