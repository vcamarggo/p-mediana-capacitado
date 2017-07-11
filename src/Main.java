import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Vinícius Camargo
 *
 * @date 10/07/2017
 */
class Main {
    private static class Vertice {
	int x;
	int y;
	short capacidade;
	short demanda;

	public Vertice(int x, int y, short capacidade, short demanda) {
	    super();
	    this.x = x;
	    this.y = y;
	    this.capacidade = capacidade;
	    this.demanda = demanda;
	}
    }

    public static void main(String[] args) throws FileNotFoundException {
	Scanner scan = new Scanner(new FileReader(Main.class.getResource("1.in").getPath()));
	// Scanner scan = new Scanner(System.in);
	int nrVertices = scan.nextInt();
	short nrMedianas = scan.nextShort();
	List<Vertice> vertices = new ArrayList<>();

	for (int i = 0; i < nrVertices; i++) {
	    vertices.add(new Vertice(scan.nextInt(), scan.nextInt(), scan.nextShort(), scan.nextShort()));
	}
	scan.close();
    }

}
