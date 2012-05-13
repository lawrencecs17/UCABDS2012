package miorquidea

import miorquidea.Comentario;
import miorquidea.Usuario;

/** 
*
* @author Ricardo Portela
* Clase destinada a gestionar todas las funcionalidades de Calificacion
*
*/

class Calificacion {
	
	Comentario comentario
	Usuario persona
	boolean like
	boolean dislike

	static belongsTo = [Comentario comentario]
	
	/**
	* Restricciones básicas de la clase
	*/
	
    static constraints = {
    }
	/**
	* Persistencia almacenada en una base de datos MongoDB
	*/
   
   static mapWith = "mongo"
}

