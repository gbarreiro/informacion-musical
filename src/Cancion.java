import java.util.ArrayList;
import java.util.Comparator;


/**
 * Clase que modela un elemento Cancion de un archivo IML
 * @author Guillermo Barreiro
 *
 */
public class Cancion {
	
	public enum Genero {Rock, Pop, Country, Balada};
	
	private String idc; // identificador tipo IDD_DISCO-XX de la canción
	private String titulo;
	private int duracion; // duración en segundos de la canción
	private Genero genero; // género de la canción
	private String comentario; // comentario adicional sobre la canción (opcional)
	
	private ArrayList<Version> versiones; // no tiene por qué haber versiones del tema...
	
	private String[] premiosDisco; // premios recibidos por el disco al que pertenece el tema (apaño chapuzas para la fase 14)
	
	/**
	 * Constructor de la clase Canción.
	 * @param idc ID de la canción
	 * @param titulo Título de la canción
	 * @param duracion Duración en segundos de la canción
	 * @param genero Género de la canción. Tiene que ser Rock, Pop, Country o Balada, no vale otro género.
	 * @param comentario (Opcional) Comentario sobre la canción
	 * @param versiones (Opcional) Lista con canciones sobre la canción
	 */
	public Cancion(String idc, String titulo, int duracion, Genero genero, String comentario, ArrayList<Version> versiones) {
		this.idc = idc;
		this.titulo = titulo;
		this.duracion = duracion;
		this.versiones = versiones;
		this.genero = genero;
		this.comentario = comentario;
	}
	
	public String getIdc() {
		return idc;
	}



	public String getTitulo() {
		return titulo;
	}



	public int getDuracion() {
		return duracion;
	}



	public Genero getGenero() {
		return genero;
	}



	public String getComentario() {
		return comentario.isEmpty()?" ": comentario.trim();
	}



	public ArrayList<Version> getVersiones() {
		return versiones;
	}
	
	
	
	public String getPremiosDisco() {
		return premiosDisco.length==0?" ":String.join(" ", this.premiosDisco);
	}

	public void setPremiosDisco(String[] premiosDisco) {
		this.premiosDisco = premiosDisco;
	}



	/**
	 * Compara dos objetos Cancion por la duración en segundos del tema, en orden ascendente.
	 * Si hay empate se desempata por orden alfabético.
	 * Se utiliza en la fase 13.
	 * @author Guillermo Barreiro
	 *
	 */
	public static class ComparaDuracion implements Comparator<Cancion>{

		@Override
		public int compare(Cancion o1, Cancion o2) {
			// Primero compara por duración
			int diferenciaDuracion = o1.getDuracion() - o2.getDuracion();
			if(diferenciaDuracion!=0) {
				return diferenciaDuracion;
			}else {
				// En caso de empate, orden alfabético del IDC
				return o1.idc.compareTo(o2.getIdc());
			}
			
		}
		
	}

	/**
	 * Compara dos objetos Cancion por orden alfabético inverso.
	 * Se utiliza en la fase 14.
	 * @author Guillermo Barreiro
	 *
	 */
	public static class ComparaAlfabeticoInverso implements Comparator<Cancion>{

		@Override
		public int compare(Cancion o1, Cancion o2) {
			return o2.getTitulo().compareTo(o1.getTitulo()); // orden alfabético inverso
			
		}
		
	}

	

}
