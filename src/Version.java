/**
 * Clase que modela una versión de una canción determinada.
 * @author Guillermo Barreiro
 *
 */
public class Version{
		private int anio; // año en el que se publicó la versión de la canción
		private String titulo; // opcional (se tiene que especificar o idc o titulo)
		private String idc; // opcional (se tiene que especificar o idc o titulo)
		private String urlIml; // URL del fichero IML en el que está el año de esta canción (no tiene por qué estar en ese fichero esta versión...)
		
		/**
		 * Constructor de la clase Version. O bien el parámetro idc o el parámetro título tienen que ser null.
		 * Se asume que esta comprobación ya se ha hecho al parsear los archivos IML.
		 * @param anio
		 * @param idc
		 * @param titulo
		 * @param urlIml
		 */
		public Version(int anio, String idc, String titulo, String urlIml) {
			this.anio = anio;
			this.titulo = titulo;
			this.idc = idc;
			this.urlIml = urlIml;
		}

		public int getAnio() {
			return anio;
		}

		public String getTitulo() {
			return titulo;
		}

		public String getIdc() {
			return idc;
		}

		public String getUrlIml() {
			return urlIml;
		}
		
		
		
	}
