
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Vinicius Camargo , Fernando Carvalho
 *
 * @date 10/07/2017
 *
 * @opt 17.288,99
 */
class Main {

    // PARAMETROS
    private static final short FACILIDADE_PAI_BOM = 50;
    private static final short FACILIDADE_MAE_BOM = 100;
    private static final int ELITISMO = 300;
    private static final float TAXA_MUTACAO = 0.05f;
    private static final short ITER_SEM_MOD = 100;
    private static final int TAM_POP = 50000;
    private static final short PENALIZACAO = Short.MAX_VALUE;
    private static final short ALEATORIEDADE_ROLETA = 1000;
    private static final short VIZINHOS_LOCAIS = 500;

    /**
     * Classe que representa o vertice que sera carregado e mantido inalterado
     *
     */
    private static class Vertice {

	private int id;
	protected double x;
	protected double y;
	private short capacidade;
	private short demanda;

	public Vertice(int id, double x, double y, short capacidade, short demanda) {
	    super();
	    this.id = id;
	    this.x = x;
	    this.y = y;
	    this.capacidade = capacidade;
	    this.demanda = demanda;
	}

	public int getId() {
	    return id;
	}

	public double getX() {
	    return x;
	}

	public double getY() {
	    return y;
	}

	public short getCapacidade() {
	    return capacidade;
	}

	public short getDemanda() {
	    return demanda;
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
		currentVal = medianas.get(iter).consultaDistancia(this); // calcula
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
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    long temp;
	    temp = Double.doubleToLongBits(x);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    temp = Double.doubleToLongBits(y);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    Vertice other = (Vertice) obj;
	    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
		return false;
	    }
	    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
		return false;
	    }
	    return true;
	}

    }

    /**
     *
     * Classe que e um vertice que representa uma mediana
     */
    private static class Mediana extends Vertice implements Cloneable {

	private double somaDasDistancias;
	private short capacidadeUsada;

	public Mediana(Vertice v) {
	    super(v.getId(), v.getX(), v.getY(), v.getCapacidade(), v.getDemanda());
	    capacidadeUsada = v.getDemanda();
	}

	/**
	 * @param vertice
	 * @return distancia entre a mediana e o vertice
	 */
	public double consultaDistancia(Vertice vertice) {
	    return vetorDistancia[this.getId()][vertice.getId()];
	}

	@Override
	protected Mediana clone() throws CloneNotSupportedException {
	    Mediana clone = (Mediana) super.clone();
	    clone.capacidadeUsada = super.getDemanda();
	    clone.somaDasDistancias = 0;
	    return clone;
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
	    acrescentaDistancia(consultaDistancia(vertice));
	    diminuiCapacidade(vertice.getDemanda());
	}

	public void setCapacidadeUsada(short demanda) {
	    capacidadeUsada = demanda;
	}

    }

    /**
     * Esta classe representa uma instancia da solucao
     *
     */
    private static class Solucao implements Comparable<Solucao>, Cloneable {

	private double qualidade;
	private List<Mediana> medianas = new ArrayList<>();

	public List<Mediana> getMedianas() {
	    return medianas;
	}

	public double getQualidade() {
	    return qualidade;
	}

	@Override
	protected Solucao clone() {
	    try {
		return (Solucao) super.clone();
	    } catch (CloneNotSupportedException e) {
		return null;
	    }
	}

	/**
	 * operador de mutacao
	 */
	public void fazMutacao(int nrMod) {
	    int index = 0;
	    int i;
	    for (i = 0; i < nrMod; ++i) {
		index = randomizer.nextInt(this.getMedianas().size());
		this.getMedianas().remove(index);
	    }

	    Vertice v = null;
	    do {
		index = randomizer.nextInt(nrVertices);
		v = vertices[index];
		if (!this.getMedianas().contains(v)) {
		    this.getMedianas().add(new Mediana(v));
		}
	    } while (this.getMedianas().size() < nrMedianas);
	    for (Mediana m : medianas) {
		m.setCapacidadeUsada(m.getDemanda());
	    }
	}

	public void avaliarQualidadeSolucao(int indicesPercorridos) {
	    double fitness = 0;
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
	 * @param pai
	 * @param mae
	 * @return duas solucoes baseadas nos parametros
	 */
	public Solucao[] fazCruzamento(Solucao pai, Solucao mae) {
	    Solucao[] filhos = new Solucao[2];
	    filhos[0] = new Solucao();
	    filhos[1] = new Solucao();

	    int randPoint = randomizer.nextInt(nrMedianas);
	    for (int i = 0; i < nrMedianas; i++) {
		try {
		    if (i < randPoint && !mae.getMedianas().contains(pai.getMedianas().get(i))
			    && !pai.getMedianas().contains(mae.getMedianas().get(i))) {

			filhos[0].getMedianas().add(mae.getMedianas().get(i).clone());
			filhos[1].getMedianas().add(pai.getMedianas().get(i).clone());
		    } else {
			filhos[0].getMedianas().add(pai.getMedianas().get(i).clone());
			filhos[1].getMedianas().add(mae.getMedianas().get(i).clone());
		    }
		} catch (CloneNotSupportedException e) {
		    e.printStackTrace();
		}
	    }

	    int indicesPercorridos0 = filhos[0].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	    filhos[0].avaliarQualidadeSolucao(indicesPercorridos0);

	    int indicesPercorridos1 = filhos[1].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	    filhos[1].avaliarQualidadeSolucao(indicesPercorridos1);

	    return filhos;
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
    private static double[][] vetorDistancia;
    static Random randomizer = new Random();

    public static void main(String[] args) throws FileNotFoundException {

	// Scanner scan = new Scanner(new
	// FileReader(Main.class.getResource("teste2.in").getPath()));
	Scanner scan = new Scanner(System.in);
	nrVertices = scan.nextInt();
	nrMedianas = scan.nextShort();
	scan.nextLine();
	vertices = new Vertice[nrVertices];
	for (int i = 0; i < nrVertices; i++) {
	    vertices[i] = new Vertice(i, Double.parseDouble(scan.next()), Double.parseDouble(scan.next()),
		    scan.nextShort(), scan.nextShort());
	}
	scan.close();

	calculaDistancias();
	System.out.println("P-Mediana-Capacitada");
	for (int i = 1; i <= 10; i++) {
	    System.out.println(
		    "Soma das distancias" + i + " : " + new DecimalFormat("#.##").format(pMedianaCapacitada(false)));
	}

	System.out.println("P-Mediana-Capacitada-Com-Busca-Local");
	for (int i = 1; i <= 10; i++) {
	    System.out.println(
		    "Soma das distancias" + i + " : " + new DecimalFormat("#.##").format(pMedianaCapacitada(true)));
	}
    }

    private static void calculaDistancias() {
	vetorDistancia = new double[nrVertices][nrVertices];
	for (int i = 0; i < vertices.length; i++) {
	    for (int j = i + 1; j < vertices.length; j++) {
		double distancia = encontraDistancia(vertices[i], vertices[j]);
		vetorDistancia[i][j] = distancia;
		vetorDistancia[j][i] = distancia;
	    }
	}

    }

    private static double encontraDistancia(Vertice v1, Vertice v2) {
	double diferencaX = v1.getX() - v2.getX();
	double diferencaY = v1.getY() - v2.getY();
	double dist = Math.pow(diferencaX, 2) + Math.pow(diferencaY, 2);
	double distance = Math.sqrt(dist);
	return distance;
    }

    private static double pMedianaCapacitada(boolean comBuscaLocal) {
	Populacao pop = new Populacao();
	Solucao[] newPop = new Solucao[TAM_POP];
	Solucao[] indiv = new Solucao[2];

	int count;

	double melhor = Double.MAX_VALUE;
	for (int nrSemMod = 0; nrSemMod < ITER_SEM_MOD;) {
	    count = 0;
	    Arrays.sort(pop.getSolucoes());

	    if (comBuscaLocal && nrSemMod % 30 == 0 && nrSemMod > 0)
		System.out.println("Sem mod: " + nrSemMod);

	    if (melhor <= pop.getSolucoes()[0].getQualidade()) {
		nrSemMod++;
	    } else {
		melhor = pop.getSolucoes()[0].getQualidade();
		nrSemMod = 0;
		// System.out.println("Melhor: " + new
		// DecimalFormat("#.##").format(melhor));
	    }

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
		    indiv[0].fazMutacao(randomizer.nextInt(nrMedianas));
		    int indicesPercorridos = indiv[0].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
		    indiv[0].avaliarQualidadeSolucao(indicesPercorridos);
		}
		if (randomizer.nextDouble() < TAXA_MUTACAO) {
		    indiv[1].fazMutacao(randomizer.nextInt(nrMedianas));
		    int indicesPercorridos = indiv[1].ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
		    indiv[1].avaliarQualidadeSolucao(indicesPercorridos);
		}
		newPop[count] = indiv[0];
		newPop[count + 1] = indiv[1];
		count += 2;
	    }
	    if (comBuscaLocal) {
		for (count = 0; count < TAM_POP; count++) {
		    pop.getSolucoes()[count] = fazBuscaLocal(pop, pop.getSolucoes()[count]);
		}
	    }
	    // Conceito de Nova Geracao Completa
	    pop.setPopulation(newPop);
	    pop.avaliarQualidadePopulacao();
	}

	return pop.encontraMelhorSolucaoDaLista().getQualidade();
    }

    private static Solucao fazBuscaLocal(Populacao pop, Solucao s) {
	Solucao newSolucao = s.clone();
	Solucao oldSolucao = s;
	boolean trocou;
	do {
	    trocou = false;
	    newSolucao = pegarMenorVizinhoLocal(oldSolucao.clone());
	    if (newSolucao.getQualidade() < oldSolucao.getQualidade()) {
		oldSolucao = newSolucao;
		trocou = true;
	    }
	} while (newSolucao.getQualidade() < oldSolucao.getQualidade() || trocou);
	return oldSolucao;
    }

    private static Solucao pegarMenorVizinhoLocal(Solucao s) {
	HashSet<Solucao> vizinhosLocais = new HashSet<>();

	for (int i = 0; i < VIZINHOS_LOCAIS; i++) {
	    Solucao sClone = s.clone();
	    sClone.fazMutacao((int) (nrMedianas * 0.1));
	    int indicesPercorridos = sClone.ligaVerticesAsMedianasERetornaUltimoIndiceUsado();
	    sClone.avaliarQualidadeSolucao(indicesPercorridos);
	    vizinhosLocais.add(sClone);
	}
	vizinhosLocais.add(s);
	return Collections.min(vizinhosLocais, null);
    }

}
