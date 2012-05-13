package miorquidea

/**
*
* @author Ricardo Portela
* Clase destinada a gestionar todas las funcionalidades de Etiqueta
*
*/
 
class Etiqueta {
	
	String nombre

	/**
	* Restricciones básicas de la clase
	*/
	
    static constraints = {  
		nombre(nombre:true, blank:false,unique:true)
    }
	/**
	* Persistencia almacenada en una base de datos MongoDB
	*/
   
   static mapWith = "mongo"
}