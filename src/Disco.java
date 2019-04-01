import java.util.ArrayList;


/**
 * Clase que modela un elemento "Disco" de un archivo IML.
 * @author Guillermo Barreiro
 *
 */
public class Disco implements Comparable<Disco>{
	
	public enum Premio {DiscoDeOro, Grammy, LamparaMinera};

	private String idd; // identificador del disco: XXXX-XXX-XXX
	private String[] idiomas; // opcional: idiomas en los que está el disco; si vale null es que está en el idoma del país en el que se grabó
	private String titulo;
	private String interprete;
	private Premio[] premios; // opcional
	private ArrayList<Cancion> canciones; // como mínimo una canción
	
	public Disco(String idd, String idiomas, String titulo, String interprete, Premio[] premios, ArrayList<Cancion> canciones) {
		this.idd = idd;
		this.idiomas = idiomas.split("//s");
		this.titulo = titulo;
		this.interprete = interprete;
		this.premios = premios;
		this.canciones = canciones;
	}
	
	public String getIdd() {
		return idd;
	}
	public String getIdiomas() {
		return String.join(" ", this.idiomas);
	}
	public String getTitulo() {
		return titulo;
	}
	public String getInterprete() {
		return interprete;
	}
	public Premio[] getPremios() {
		return premios;
	}
	public ArrayList<Cancion> getCanciones() {
		return canciones;
	}

	@Override
	public int compareTo(Disco o) {
		// Primero comparamos alfabéticamente por intérprete
		int compInterprete = this.interprete.compareTo(o.getInterprete());
		if(compInterprete!=0) {
			return compInterprete;
		}else {
			// Empate entre intérpretes, desempatamos por nombre del álbum
			return this.titulo.compareTo(o.getTitulo());
		}
	}
	
	
	
}
