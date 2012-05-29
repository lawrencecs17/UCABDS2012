package miorquidea

import java.util.Date;
import grails.converters.XML

/**
* Clase destinada a dar respuesta al cliente de las operaciones fallidas
* y de advertencia por parte del servidor con respecto a la calificacion
* de los comentario
* @author Ricardo Portela
*
*/
class CalificacionServidor {

	String Like
	String Dislike
	String Comentados

    static constraints = {
    }
	
	/**
	* Mensaje cuando la consulta a la Base de Datos no devuelve documentos
	*/
}