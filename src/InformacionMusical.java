import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Servlet Tomcat que ejecutará el servicio de información musical.
 * @author Guillermo Barreiro
 *
 */
public class InformacionMusical extends HttpServlet {

	/**
	 * Mapa que almacena cada fichero IML como un DOM. La clave es el nombre del fichero IML original.
	 */
	private HashMap<String, Document> ficherosIML;

	/**
	 * Mapa que almacena el nombre del fichero IML correspondiente a cada año.
	 */
	private HashMap<String, String> anios;

	/**
	 * Mapa que almacena todas las advertencias de los ficheros IML originales.
	 * La clave es la URL del fichero IML, y el valor es una lista con todas las warnings para ese fichero.
	 */
	private LinkedHashMap<String, LinkedList<String>> warnings;

	/**
	 * Mapa que almacena todos los errores de los ficheros IML originales.
	 * La clave es la URL del fichero IML, y el valor es una lista con todas los errores para ese fichero.
	 */
	private LinkedHashMap<String, LinkedList<String>> errores;

	/**
	 * Mapa que almacena todos los errores fatales de los ficheros IML originales.
	 * La clave es la URL del fichero IML, y el valor es una lista con todas los errores para ese fichero.
	 */
	private LinkedHashMap<String, LinkedList<String>> erroresFatales;

	/**
	 * Contraseña necesaria para ejecutar la aplicación.
	 */
	private static final String PASSWORD = "pwd";

	/**
	 * Inicio de todos los formularios. Crea un formulario que envíe los datos mediante GET al servlet.
	 */
	private static final String INICIO_FORM = "<form action=\"IM\" method=\"get\">";

	/**
	 * Final de todos los formularios. Etiqueta HTML que cierra el formulario previamente abierto.
	 */
	private static final String FINAL_FORM = "</form>";

	/**
	 * Botón de enviar en todas las pantallas. Hay que meterlo dentro de un elemento <b>form</b>
	 */
	private static final String ENVIAR_FORM = "<br><input type=\"submit\" value=\"Enviar\" id=\"boton_enviar\"/>";

	/**
	 * Pie de página con el nombre del autor. Cierra el documento HTML.
	 */
	private static final String FOOTER_HTML = "</body><footer><br><br>© Guillermo Barreiro 2018</footer></html>";

	/**
	 * URL del schema XSD.
	 */
	private static String URL_XSD = "iml.xsd";

	/**
	 * URL del archivo IML base, a partir del cual se van descubriendo el resto a través de las Versiones
	 */
	private static final String FICHERO_IML_INICIAL = "";


	/**
	 * Inicializa el servlet.
	 * Lee los archivos IML y rellena con su contenido la BD
	 */
	public void init (ServletConfig config) {
		// Empezamos leyendo el fichero inicial, y a partir de él sacamos el resto
		URL_XSD = config.getServletContext().getRealPath("iml.xsd");
		cargarDocumentos(FICHERO_IML_INICIAL);
		// A partir de aquí, ya podemos procesar peticiones GET de los clientes

	}

	/**
	 * Método llamado cada vez que se recibe una petición HTTP GET.
	 * Comprueba la contraseña, y si es correcta ejecuta el método correspondiente a la fase solicitada.
	 * @throws IOException
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Parámetros GET
		String passwd = request.getParameter("p");
		String pfase = request.getParameter("pfase");
		boolean auto = request.getParameter("auto")==null? false : request.getParameter("auto").equalsIgnoreCase("si"); // determina si hay que devolver un HTML con la información presentada (false) o un XML (true)
		String panio; // opcional
		String pidd; // opcional
		String pidc; // opcional

		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		// 1. Imprimimos la head HTML / XML y configuramos el tipo de respuesta
		if(auto) {
			// Cabecera XML
			response.setContentType("text/xml");
			out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		}else {
			// Cabecera HTML
			response.setContentType("text/html");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servicio de información musical</title>"); // título de la página
			out.println("<meta charset=\"utf-8\"/>"); // codificación de la página (UTF-8)
			out.println("<LINK rel=\"stylesheet\" href=\"iml.css\">"); // hoja de estilos CSS: p2.css
			out.println("</head>");

			out.println("<body><h1>Servicio de información musical</h1>");
		}

		// 2. Comprobamos la contraseña
		if(passwd==null) {
			// No se ha especificado contraseña
			if(auto) {
				// Respuesta XML
				out.println("<wrongRequest>no passwd</wrongRequest>");
			}else {
				// Respuesta HTML
				out.println("No se ha especificado ninguna contraseña, inténtalo de nuevo");
				out.println(FOOTER_HTML);

			}
			out.close();
			return;
		}else if(!passwd.equals(InformacionMusical.PASSWORD)) {
			// Contraseña incorrecta
			if(auto) {
				// Respuesta XML
				out.println("<wrongRequest>bad passwd</wrongRequest>");
			}else {
				// Respuesta HTML
				out.println("Contraseña incorrecta, inténtalo de nuevo");
				out.println(FOOTER_HTML);
			}
			out.close();
			return;
		}

		// Contraseña correcta:

		// 3. Ejecutamos el método correspondiente a la fase solicitada
		if(pfase==null) {
			// Si no se ha especificado ninguna fase, por defecto arrancaremos desde la 01
			doGetFase01(auto, out);
		}else {
			switch(pfase) {
			case "01":
				// Fase 01: Menú principal
				doGetFase01(auto, out);
				break;
			case "02":
				// Fase 02: Mostrar ficheros erróneos
				doGetFase02(auto, out);
				break;
			case "11":
				// Fase 11: Mostrar la lista de años
				doGetFase11(auto, out);
				break;
			case "12":
				// Fase 12: Mostrar los discos del año seleccionado

				// Parámetros para la fase 12
				panio = request.getParameter("panio");

				doGetFase12(auto, out, panio);

				break;

			case "13":
				// Fase 13: Mostrar las canciones del disco seleccionado

				// Parámetros para la fase 13
				panio = request.getParameter("panio");
				pidd = request.getParameter("pidd");

				doGetFase13(auto, out, panio, pidd);

				break;

			case "14":
				/* Fase 14: Mostrar las canciones del intérprete del disco seleccionado que duren menos
				 	que la canción seleccionada en la fase anterior
				 */

				// Parámetros para la fase 14
				panio = request.getParameter("panio");
				pidd = request.getParameter("pidd");
				pidc = request.getParameter("pidc");

				doGetFase14(auto, out, panio, pidd, pidc);

				break;
			default:
				// Si la fase es errónea, se empieza por la fase 01
				doGetFase01(auto, out);
				break;
			}
		}

		// 4. Imprime el pie de página y cierra el documento (solo en HTML)
		if(!auto) {
			out.println(FOOTER_HTML);
		}

		out.close();
	}

	// Métodos de fase

	/**
	 * Fase 01: pantalla para elegir entre la lista de ficheros erróneos o proceder a una consulta
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada
	 */
	private void doGetFase01(boolean auto, PrintWriter out) {
		if(auto) {
			// XML
			out.println("<service> <status>OK</status> </service>");
		}else {
			// HTML
			out.println("<h2>Bienvenido</h2> ");
			out.println("<h3>Parámetros: </h3>");
			out.println("<b>Comentario:</b> " + this.comentario + "<br>");
			out.println("<b>Género:</b> " + this.genero + "<br>");
			out.println("<b>Idioma:</b> " + this.lang + "<br><br>");
			out.println("<br><a href=\"P2IM?pfase=02&p=" + PASSWORD + "\">Listar ficheros IML erróneos</a>"); // Opción para mostrar los ficheros erróneos (fase 02)
			out.println("<br><h3>Selecciona una consulta:</h3> " // Opción para mostrar la lista de años (fase 11)
					+ INICIO_FORM
					+ "<input type=\"radio\" name=\"pfase\" value=\"11\" checked/> <b>Consulta 1:</b> Canciones de un intérprete que duran menos que una dada <br><br>" // ?pfase=11
					+ "<input type=\"hidden\" name=\"p\" value=\"" + PASSWORD + "\"/>"
					+ ENVIAR_FORM + FINAL_FORM);
		}
	}

	/**
	 * Fase 02: ficheros IML que el parser ha marcado como erróneos
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada
	 */
	private void doGetFase02(boolean auto, PrintWriter out) {
			out.println(auto?"<errores>":"<h2>Ficheros erróneos</h2><br>");

			// Imprime los warnings
			out.println(auto?"<warnings>":"<h3>Se han encontrado " + warnings.size() + " ficheros con warnings:</h3><br><ul>");
			for(String urlFichero: warnings.keySet()) {
				// Muestra los warnings de cada fichero
				out.println(auto?"<warning>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = warnings.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo una a una todas las warnings
					out.println(auto?detalle:"<li>" + detalle);
				}
				out.println(auto?"</cause></warning>":"</ul><br>");

			}
			out.println(auto?"</warnings>":"</ul><br>");

			// Imprime los errores
			out.println(auto?"<errors>":"<h3>Se han encontrado " + errores.size() + " ficheros con errores:</h3><br><ul>");
			for(String urlFichero: errores.keySet()) {
				// Muestra los errores de cada fichero
				out.println(auto?"<error>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = errores.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo uno a uno todos los errores
					out.println(auto?detalle:"<li>" + detalle + "<br>");
				}
				out.println(auto?"</cause></error>":"</ul><br>");

			}
			out.println(auto?"</errors>":"</ul><br>");

			// Imprime los fatal errors
			out.println(auto?"<fatalerrors>":"<h3>Se han encontrado " + erroresFatales.size() + " ficheros con errores fatales:</h3><br><ul>");
			for(String urlFichero: erroresFatales.keySet()) {
				// Muestra los errores fatales de cada fichero
				out.println(auto?"<fatalerror>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = erroresFatales.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo uno a uno todos los errores fatales
					out.println(auto?detalle:"<li>" + detalle + "<br>");
				}
				out.println(auto?"</cause></fatalerror>":"</ul><br>");
			}

			out.println(auto?"</fatalerrors>":"</ul><br>");

			if(auto) {
				// Cierra el cuerpo XML
				out.println("</errores>");
			}else {
				// Botón de atrás en HTML (lleva a la fase 01)
				out.println(getBotonAtras(PASSWORD));
			}


	}

	/**
	 * Fase 11: pantalla para elegir el año en el que está el disco que se quiere consultar
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada
	 */
	private void doGetFase11(boolean auto, PrintWriter out) {
		out.println(auto?"<anios>":"<h2>Selecciona un año: </h2><br>");
		if(!auto) { // HTML:
			// Hay que crear un formulario
			out.println(INICIO_FORM);

			// Vamos creando un radiobutton para cada año
			boolean checked = false;
			for(String year: getC1Anios()) {
				out.println("<input type=\"radio\" name=\"panio\" class=\"form_input\" value=\"" + year + "\"" + (checked?">":" checked>") + year + "<br>");
				checked = true; // solo el primer radiobutton estára seleccionado
			}

			// Parámetros para pasar a la siguiente fase
			out.println("<input type=\"hidden\" name=\"p\" value=\"" + PASSWORD + "\"/>"); // Contraseña necesaria para todas las fases
			out.println("<input type=\"hidden\" name=\"pfase\" value=\"12\"/><br>"); // El botón "Enviar" lleva a la siguiente fase (12)
			out.println(ENVIAR_FORM + FINAL_FORM + getBotonAtras(PASSWORD));

		}else { // XML
			for(String year: getC1Anios()) {
				// Imprime año a año
				out.println("<anio>" + year + "</anio>");
			}
			out.println("</anios>");
		}

	}

	/**
	 * Fase 12: pantalla para elegir un álbum publicado en el año seleccionado
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada.
	 * @param panio Año seleccionado en la fase 11.
	 */
	private void doGetFase12(boolean auto, PrintWriter out, String panio) {
		// 1. Comprobamos que el parámetro año sea correcto
		if(!comprobarParametro("panio", panio, auto, out)) {
			// No se ha especificado ?panio
			return;
		}

		out.println(auto?"<discos>":"<h2>Selecciona un disco: </h2><br><h3>Año " + panio + "</h3><br>");
		if(!auto) { // HTML:
			// Hay que crear un formulario
			out.println(INICIO_FORM);

			// Vamos creando un radiobutton para cada disco
			boolean checked = false;
			int posicion = 0;
			for(Disco disco: getC1Discos(panio)) {
				posicion++;
				out.println("<input type=\"radio\" name=\"pidd\" class=\"form_input\" value=\"" + disco.getIdd() + "\"" + (checked?">":" checked>")
						+ posicion + ". <b>Título: </b>" + disco.getTitulo() + "; <b>IDD: </b>" + disco.getIdd()
						+ "; <b>Intérprete: </b>" + disco.getInterprete() + "; <b>Idiomas: </b>" + disco.getIdiomas() + "<br>");
				checked = true; // solo el primer radiobutton estára seleccionado
			}

			// Parámetros para pasar a la siguiente fase
			out.println("<input type=\"hidden\" name=\"p\" value=\"" + PASSWORD + "\"/>"); // Contraseña necesaria para todas las fases
			out.println("<input type=\"hidden\" name=\"pfase\" value=\"13\"/>"); // El botón "Enviar" lleva a la siguiente fase (13)
			out.println("<input type=\"hidden\" name=\"panio\" value=\"" + panio + "\"/>"); // Parámetro ?panio de la fase anterior necesario
			out.println(ENVIAR_FORM + FINAL_FORM + getBotonesAtrasInicio("11",PASSWORD, null, null));

		}else { // XML
			for(Disco disco: getC1Discos(panio)) {
				// Imprime disco a disco
				out.printf("<disco idd=\"%s\" interprete=\"%s\" langs=\"%s\">%s</disco>\n", disco.getIdd(), disco.getInterprete(), disco.getIdiomas(), disco.getTitulo());
			}
			out.println("</discos>");
		}


	}

	/**
	 * Fase 13: pantalla para elegir una canción del disco seleccionado en la fase 12
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada.
	 * @param panio Año seleccionado en la fase 11.
	 * @param pidd ID del disco seleccionado en la fase 12.
	 */
	private void doGetFase13(boolean auto, PrintWriter out, String panio, String pidd) {
		// 1. Comprobamos que los parámetros año y pidd sean correctos
		if(!comprobarParametro("panio", panio, auto, out) || !comprobarParametro("pidd", pidd, auto, out)) {
			// No se ha especificado alguno de los parámetros obligatorios
			return;
		}

		out.println(auto?"<canciones>":"<h2>Selecciona una canción: </h2><br><h3>Año: " + panio + "<br>Disco: " + pidd +
				"</h3><br>");
		if(!auto) { // HTML:
			// Hay que crear un formulario
			out.println(INICIO_FORM);

			// Vamos creando un radiobutton para cada disco
			boolean checked = false;
			int posicion = 0;
			for(Cancion cancion: getC1Canciones(panio,pidd)) {
				posicion++;
				out.println("<input type=\"radio\" name=\"pidc\" class=\"form_input\" value=\"" + cancion.getIdc() + "\"" + (checked?">":" checked>")
						+ posicion + ". <b>Título: </b>" + cancion.getTitulo() + "; <b>IDC: </b>" + cancion.getIdc()
						+ "; <b>Género: </b>" + cancion.getGenero() + "; <b>Duración: </b>" + cancion.getDuracion() + "<br>");
				checked = true; // solo el primer radiobutton estára seleccionado
			}

			// Parámetros para pasar a la siguiente fase
			out.println("<input type=\"hidden\" name=\"p\" value=\"" + PASSWORD + "\"/>"); // Contraseña necesaria para todas las fases
			out.println("<input type=\"hidden\" name=\"pfase\" value=\"14\"/>"); // El botón "Enviar" lleva a la siguiente fase (13)
			out.println("<input type=\"hidden\" name=\"panio\" value=\"" + panio + "\"/>"); // Parámetro ?panio de la fase anterior necesario
			out.println("<input type=\"hidden\" name=\"pidd\" value=\"" + pidd + "\"/>"); // Parámetro ?pidd de la fase anterior necesario
			out.println(ENVIAR_FORM + FINAL_FORM + getBotonesAtrasInicio("12",PASSWORD, panio, null));

		}else { // XML
			for(Cancion cancion: getC1Canciones(panio,pidd)) {
				// Imprime canción a canción
				out.printf("<cancion idc=\"%s\" genero=\"%s\" duracion=\"%s\">%s</cancion>\n", cancion.getIdc(),
						cancion.getGenero(), cancion.getDuracion(), cancion.getTitulo());
			}
			out.println("</canciones>");
		}

	}

	/**
	 * Fase 14: pantalla que mostrará las canciones del intérprete del disco seleccionado en la fase 12 cuya duración sea menor que la canción elegida en la fase 13.
	 * @param auto true: devolverá un simple archivo XML con la información solicitada, false: devolverá un HTML estructurado y decorado con la info solicitada.
	 * @param panio Año seleccionado en la fase 11.
	 * @param pidd ID del disco seleccionado en la fase 12.
	 * @param pidc ID de la canción seleccionada en la fase 13.
	 */
	private void doGetFase14(boolean auto, PrintWriter out, String panio, String pidd, String pidc) {
		// 1. Comprobamos que los parámetros año y pidd sean correctos
		if(!comprobarParametro("panio", panio, auto, out) || !comprobarParametro("pidd", pidd, auto, out) || !comprobarParametro("pidc", pidc, auto, out)) {
			// No se ha especificado alguno de los parámetros obligatorios
			return;
		}

		out.println(auto?"<songs>":"<h2>Resultado: </h2><br><h3>Año: " + panio + "<br>Disco: " +
				pidd + "<br>Canción: " + pidc + "</h3><br>");
		if(!auto) { // HTML:
			// Hay que mostar una lista de elementos (ol en HTML)
			out.println("<ol>");

			// Un elemento por canción
			for(Cancion cancion: getC1Resultado(panio,pidd,pidc)) {
				out.println("<li> <b>Título: </b>" + cancion.getTitulo() + "; <b>Descripción: </b>" + cancion.getComentario()
						+ "; <b>Premios: </b>" + cancion.getPremiosDisco() + "<br>");
			}

			out.println("</ol>" + getBotonesAtrasInicio("13",PASSWORD, panio, pidd));

		}else { // XML
			for(Cancion cancion: getC1Resultado(panio,pidd,pidc)) {
				// Imprime canción a canción
				out.printf("<song descripcion=\"%s\" premios=\"%s\">%s</song>\n", cancion.getComentario(),
						cancion.getPremiosDisco(), cancion.getTitulo());
			}
			out.println("</songs>");
		}

	}

	// Métodos de consulta

	/**
	 * Devuelve una lista con todos los años de los que se dispone de información
	 * @return ArrayList con los años ordenados cronológicamente, en Strings
	 */
	private ArrayList<String> getC1Anios(){
		ArrayList<String> years = new ArrayList<String>(anios.keySet());
		Collections.sort(years);
		return years;
	}

	/**
	 * Devuelve una lista con todos los discos que se han grabado en un determinado año
	 * @param anio
	 * @return
	 */
	private ArrayList<Disco> getC1Discos(String anio){
		XPath xpath = XPathFactory.newInstance().newXPath(); // Usaremos XPath para obtener los discos de un año
		Document documentoIML = ficherosIML.get(anios.get(anio)); // Document correspondiente al año solicitado
		ArrayList<Disco> listaDiscos = new ArrayList<Disco>(); // Lista en la que guardaremos los discos encontrados
		NodeList paises = null;
		try {
			paises = (NodeList) xpath.evaluate("Pais", documentoIML.getDocumentElement(), XPathConstants.NODESET); // obtiene todos los países de los que se dispone info
		} catch (XPathExpressionException e) {
			// Expresión XPath incorrecta (no debería darse nunca este error)
			e.printStackTrace();
			return null;
		}

		for(int i=0; i<paises.getLength(); i++) {
			// Analiza país a país
			Element pais = (Element) paises.item(i);
			String idiomaPais = pais.getAttribute("lang"); // si un álbum no especifica idiomas, se tomará el idioma del país como idioma por defecto
			NodeList discos = pais.getChildNodes(); // Obtiene la lista de discos
			for(int j=0; j<discos.getLength(); j++) {
				// Procesa disco a disco
				if(discos.item(j).getNodeType()==Node.ELEMENT_NODE) {
					Element disco = (Element)discos.item(j);

					// Obtiene las características del disco
					String idd = disco.getAttribute("idd");
					String langs = disco.getAttribute("langs"); // puede estar vacío; en ese caso usaremos el idioma del país
					String tituloDisco = disco.getElementsByTagName("Titulo").item(0).getTextContent();
					String interprete = disco.getElementsByTagName("Interprete").item(0).getTextContent();


				}

			}
		}

		Collections.sort(listaDiscos);
		return listaDiscos;
	}

	/**
	 * Devuelve las canciones del disco especificado
	 * @param anio
	 * @param idd
	 * @return
	 */
	private ArrayList<Cancion> getC1Canciones(String anio, String idd){
		XPath xpath = XPathFactory.newInstance().newXPath(); // Usaremos XPath para obtener las canciones de un disco
		Document documentoIML = ficherosIML.get(anios.get(anio)); // Document correspondiente al año solicitado
		ArrayList<Cancion> listaCanciones = new ArrayList<Cancion>(); // Lista en la que guardaremos las canciones encontradas
		NodeList canciones = null;

		try {
			canciones = (NodeList) xpath.evaluate("//Disco[@idd='" + idd + "']/Cancion", documentoIML.getDocumentElement(), XPathConstants.NODESET); // obtiene todas las canciones que pertenezcan al álbum
		} catch (XPathExpressionException e) {
			// Expresión XPath incorrecta (no debería darse nunca este error)
			e.printStackTrace();
			return null;
		}

		for(int i=0; i<canciones.getLength(); i++) {
			// Analiza canción a canción
			Element cancion = (Element) canciones.item(i);

			// Obtiene las propiedades de cada canción
			String idc = cancion.getAttribute("idc");
			String tituloCancion = cancion.getElementsByTagName("Titulo").item(0).getTextContent();
			int duracion = Integer.parseInt(cancion.getElementsByTagName("Duracion").item(0).getTextContent());
			String genero = cancion.getElementsByTagName("Genero").item(0).getTextContent();

		}

		Collections.sort(listaCanciones, new Cancion.ComparaDuracion());
		return listaCanciones;

	}

	/**
	 * Devuelve las canciones del intérprete del disco seleccionado cuya duración sea menor que la canción seleccionada.
	 * @param anio
	 * @param idd
	 * @param idc
	 * @return
	 */
	private ArrayList<Cancion> getC1Resultado(String anio, String idd, String idc){
		XPath xpath = XPathFactory.newInstance().newXPath(); // Usaremos XPath para obtener las canciones que cumplan el requisito de duración
		ArrayList<Cancion> listaCanciones = new ArrayList<Cancion>(); // Lista en la que guardaremos las canciones encontradas

		// Obtiene la duración y el nombre del intérprete de la canción seleccionada por el usuario
		String interprete, duracion = null;
		try {
			NodeList elementoDuracion = (NodeList) xpath.evaluate("//Cancion[@idc='" + idc + "']/Duracion", ficherosIML.get(anios.get(anio)).getDocumentElement(), XPathConstants.NODESET);
			duracion = elementoDuracion.item(0).getTextContent(); // podemos guardarlo como String puesto que formará parte de una expresión XPath
			NodeList elementoInterprete = (NodeList) xpath.evaluate("//Disco[@idd='" + idd + "']/Interprete", ficherosIML.get(anios.get(anio)).getDocumentElement(), XPathConstants.NODESET);
			interprete = elementoInterprete.item(0).getTextContent();
		}catch(XPathExpressionException e1) {
			// Expresión Xpath incorrecta (no debería darse nunca este error)
			e1.printStackTrace();
			return null;
		}


		// Busca año por año canciones del intérprete que duren menos que la canción seleccionada en la fase anterior
		for(Document year: ficherosIML.values()) {
			NodeList canciones = null;
			try {
				canciones = (NodeList)xpath.evaluate("//Disco[Interprete='" + interprete + "']/Cancion[Duracion < " + duracion + "]", year, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// Expresión Xpath incorrecta (no debería darse nunca este error)
				e.printStackTrace();
				return null;
			}

			for(int i=0; i<canciones.getLength(); i++) {
				// Analiza canción a canción
				Element cancion = (Element) canciones.item(i);

				// Obtiene las propiedades de cada canción
				String tituloCancion = cancion.getElementsByTagName("Titulo").item(0).getTextContent();
				NodeList hijosCancion = cancion.getChildNodes();

				// Puede que la canción tenga un comentario
				StringBuffer comentario = new StringBuffer(128);
				for(int j=0; j<hijosCancion.getLength(); j++) {
					// Busca un elemento de tipo "TEXT", que será el comentario
					if(hijosCancion.item(j).getNodeType()==Node.TEXT_NODE) {
						comentario.append(hijosCancion.item(j).getTextContent());
					}
				}

				// Puede que la canción esté premiada
				NodeList premiosCancion = ((Element)cancion.getParentNode()).getElementsByTagName("Premio");
				String[] premios = new String[premiosCancion.getLength()];
				for(int j=0; j<premios.length; j++) {
					premios[j] = premiosCancion.item(j).getTextContent().trim();
				}


				// Crea el objeto Cancion y lo añade a la lista
				Cancion objetoCancion = new Cancion(null, tituloCancion, 0, null, comentario.toString(), null); // para esta función no nos hacen falta el IDC, la duración ni el género
				objetoCancion.setPremiosDisco(premios);
				listaCanciones.add(objetoCancion);

			}

		}

		Collections.sort(listaCanciones, new Cancion.ComparaAlfabeticoInverso());
		return listaCanciones;
	}

	// Métodos para cargar los IML:

	/**
	 * A partir de un fichero IML base, carga el resto de ficheros como objetos tipo Document en #ficherosIML
	 * @param urlFicheroBase URL del fichero a partir del cual empezaremos la exploración
	 */
	private void cargarDocumentos(String primerFichero){
		// Parámetros para establecer el schema
		final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
		final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
		final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

		ficherosIML = new HashMap<String, Document>(); // inicializa el árbol de ficheros IML...
		anios = new HashMap<String, String>(); // ... y un registro de como se llama el fichero de cada año
		warnings = new LinkedHashMap<String, LinkedList<String>>(); // inicializa el registro de warnings...
		errores = new LinkedHashMap<String, LinkedList<String>>(); // ... el de errores...
		erroresFatales = new LinkedHashMap<String, LinkedList<String>>(); // ... el de errores fatales

		// Crea un parser que utilice el schema XSD
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		dbf.setAttribute(JAXP_SCHEMA_SOURCE, URL_XSD);

		try {
			leerDoc(dbf, primerFichero);
		}catch(ParserConfigurationException e1) {
			e1.printStackTrace();
		}



	}

	/**
	 * Lee un fichero IML, analizándolo y almacenándolo.
	 * Las warnings y errores se almacenan en #warnings y #errores.
	 * Los ficheros correctos se parsean y se almacenan como objetos tipo Document en #ficherosIML.
	 * Por último se guarda una correspondencia de año y nombre de fichero IML en #anios.
	 * @param factory
	 * @param urlFicheroBase URL del fichero IML a analizar
	 * @throws ParserConfigurationException
	 */
	private void leerDoc(DocumentBuilderFactory factory, String urlFicheroBase) throws ParserConfigurationException {
		final String ROOT = ""; // Ubicación de todo aquel archivo que no tenga nombre
		DocumentBuilder dbuilder = factory.newDocumentBuilder();
		XMLErrorHandler errorHandler = new XMLErrorHandler(urlFicheroBase); // se encarga de validar la sintaxis XML del documento (no el schema!)
		dbuilder.setErrorHandler(errorHandler);
		Document documentoBase = null;
		try {
			documentoBase = dbuilder.parse(new InputSource(new URL(urlFicheroBase).openStream())); // lee el fichero IML base,...
		}catch(SAXException e1) {
			// Documento con malformación XML
			LinkedList<String> malformacion = new LinkedList<String>();
			malformacion.add(e1.getMessage());
			erroresFatales.put(urlFicheroBase, malformacion);

		}catch(IOException e2) {
			// El fichero no existe, pasamos al siguiente fichero...
		}

		if(documentoBase!=null) {
			// Formato XML correcto (puede que no cumpla con el schema!)
			if(errorHandler.isXMLCorrecto()) {
				// Schema correcto
				String year = documentoBase.getElementsByTagName("Anio").item(0).getTextContent(); // ... obtiene el año al que pertenece el fichero...
				if(year!=null) {
					String[] partes = urlFicheroBase.split("/");
					String nombreFichero = partes[partes.length-1]; // ... usa como clave el nombre del fichero (no su URL completa)...
					ficherosIML.put(nombreFichero, documentoBase); // ... y lo almacena en el árbol
					anios.put(year, nombreFichero);

					// A partir del fichero base obtenemos todas sus referencias a otros ficheros IML
					NodeList referencias = documentoBase.getElementsByTagName("IML");
					LinkedList<String> iml = new LinkedList<String>(); // archivos IML que vamos a tener que analizar
					for(int i=0;i<referencias.getLength();i++) {
						String fichero = referencias.item(i).getTextContent(); // fichero IML, tal como viene en el XML: puede ser un nombre de archivo o una ruta
						String urlCompleta = fichero.startsWith("http://")||fichero.startsWith("https://")? fichero: ROOT+fichero; // URL completa del fichero (localización + nombre)
						partes = fichero.split("/");
						nombreFichero = partes[partes.length-1]; // Nombre del fichero (parte final de la URL completa)
						if(!iml.contains(urlCompleta) && !ficherosIML.containsKey(nombreFichero)) {
							// No se había tomado nota de este fichero aún: lo anotamos
							iml.add(urlCompleta); // aquí hay que añadir la URL completa
							System.out.println("IML: " + fichero);
						}

					}

					for(String url: iml) {
						// Repite el proceso con cada uno de los diferentes ficheros IML nuevos que hemos descubierto en la fase anterior
						leerDoc(factory, url);
					}
				}
			}


		}

	}

	/**
	 * Se encarga de anotar cada advertencia o error producido al parsear un fichero XML en #warnings o #errores
	 *
	 */
	class XMLErrorHandler extends DefaultHandler{

		private String nombreFichero;
		private boolean correcto = true;

		XMLErrorHandler(String ficheroIML){
			this.nombreFichero = ficheroIML;
		}

		public void warning(SAXParseException spe) {
			if(warnings.get(nombreFichero)==null) {
				warnings.put(nombreFichero, new LinkedList<String>());
			}
			warnings.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		public void error(SAXParseException spe) {
			if(errores.get(nombreFichero)==null) {
				errores.put(nombreFichero, new LinkedList<String>());
			}
			errores.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		public void fatalError(SAXParseException spe) {
			if(erroresFatales.get(nombreFichero)==null) {
				erroresFatales.put(nombreFichero, new LinkedList<String>());
			}
			erroresFatales.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		/**
		 * Informa de si el fichero está correcto o presenta algún warning o error.
		 * @return true si correcto, false si hay algún problema.
		 */
		public boolean isXMLCorrecto() {
			return correcto;
		}

	}


	// Métodos auxiliares:

	/**
	 * Comprueba si el parámetro {nombre} es correcto (no está vacío).
	 * Si es así, devuelve true; sino devuelve false e imprime en la salida HTML/XML un mensaje de error.
	 * @param nombre
	 * @param valor
	 * @param auto
	 * @param out
	 * @return {true} si es correcto, {false} si el parámetro está vacío
	 */
	private static boolean comprobarParametro(String nombre, String valor, boolean auto, PrintWriter out) {
		if(valor==null) {
			// No se ha especificado el parámetro
			if(auto) {
				// Mensaje de error en XML
				out.println("<wrongRequest>no param:" + nombre + "</wrongRequest>");
			}else {
				// Mensaje de error en HTML
				out.println("Falta el parámetro año (?" + nombre + "). Inténtelo de nuevo.<br>");
			}
		}
		return valor!=null;
	}

	/**
	 * Devuelve el código HTML con los botones de Atrás e Inicio para las diferentes fases.
	 * @param faseAtras Fase a la que llevará el botón "Atrás" (?pfase)
	 * @param password Contraseña (?p)
	 * @param panio <i>Opcional:</i> Año, necesario si la faseAtras es la 12 o 13 (?panio)
	 * @param pidd <i>Opcional:</i> ID de disco, necesario si la faseAtras es la 13 (?pidd)
	 * @return código HTML para mostrar ambos botones dentro de un formulario
	 */
	private static String getBotonesAtrasInicio(String faseAtras, String password, String panio, String pidd) {
		String anioParam = panio!=null? "<input type=\"hidden\" name=\"panio\" value=\"" + panio + "\"/>" : "";
		String iddParam = pidd!=null? "<input type=\"hidden\" name=\"pidd\" value=\"" + pidd + "\"/>" : "";
		return INICIO_FORM +
							/* Parámetros obligatorios: */
				"<input type=\"hidden\" name=\"p\" value=\"" + password + "\"/>" +
				"<input type=\"hidden\" name=\"pfase\" value=\"" + faseAtras + "\"/>" +
							/* Parámetros opcionales:*/
				 anioParam +
				 iddParam +
							/* Botón atrás:*/
							"<input type=\"submit\" id=\"boton_atras\" value=\"Atrás\"/>" + FINAL_FORM +

				INICIO_FORM +
				"<input type=\"hidden\" name=\"p\" value=\"" + password + "\"/>" +
							/* Botón inicio:*/
				"<input type=\"submit\" id=\"boton_home\" value=\"Inicio\"/>" +
				FINAL_FORM;
	}

	/**
	 * Devuelve el código HTML de un formulario para ir a la fase inicial (la 01)
	 * @param password Contraseña (?p)
	 * @return código HTML
	 */
	private static String getBotonAtras(String password) {
		return INICIO_FORM + "<input type=\"hidden\" name=\"p\" value=\"" + password + "\"/>" +"<input type=\"submit\" id=\"boton_atras\" value=\"Atrás\"/>" + FINAL_FORM;
	}



}
