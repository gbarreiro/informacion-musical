# Servicio de información musical
Proyecto que hice para la asignatura Servicios de Internet de Teleco. Se trata de un servicio web que permite explorar un catálogo musical, en el que se almacenan álbumes clasificados por año y país. Cada álbum incluye a su vez una serie de canciones. Mediante este servicio, el usuario puede seleccionar una canción y obtener aquellos temas del mismo artista que duren menos que la seleccionada.

# Funcionamiento
El proyecto es un servlet para Apache Tomcat, que almacena la información en ficheros IML, un metalenguaje XML definido en el archivo `iml.xsd`. Al inicializarse, el servlet partirá de un fichero IML base (`InformacionMusical.FICHERO_IML_INICIAL`), el cual está en blanco, puesto que por motivos de propiedad intelectual no puedo subir aquí los archivos que nos proporcionaron en la universidad para realizar este proyecto. A partir de dicho documento base, se podrán ir descubriendo recursivamente nuevos archivos, a partir de las referencias en el campo `/Songs/Pais/Disco/Cancion/Version/IML` de un fichero IML.

Una vez cargada esta información en el servlet, éste podrá resolver las consultas de los usuarios a través de una interfaz web (HTML+CSS) o mediante respuestas XML.

# Utilización
Una vez compilado y desplegado el servlet en Tomcat, se podrá acceder a él a través del navegador web, usando como contraseña "pwd": `/IM?p=pwd`. Para obtener las respuestas en XML, añadir el parámetro `?auto=si` a la URL. Para desplegar el servicio en Tomcat, se puede utilizar el archivo `web.xml` aquí presente.

