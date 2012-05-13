package miorquidea

import grails.converters.XML

/**
 * Clase destinada a dar respuesta al cliente de las operaciones fallidas
 * y de advertencia por parte del servidor
 * @author Lawrence Cermeño
 *
 */ 

class RespuestaServidor {
	
	String mensaje
	Date fecha
	boolean datos

    static constraints = {
    }
	
	/**
	* Mensaje cuando la consulta a la Base de Datos no devuelve documentos
	*/
}
