package miorquidea

/** 
 * 
 * @author Lawrence Cermeño
 * Clase destinada a gestionar todas las funcionalidades de los usuarios 
 *  
 */

class Usuario {
	 
	String nombre
	String apellido
	String nickname
	String password
	String email
	Date   fechaRegistro
	String biografia
	String pais
	boolean activo
	List<Token> tokens = []
	/**
	 * Arreglo de Tokens del usuario
	 * aqui se almacenan las foraneas con dicha clase gracias a las bondades de
	 * GORM
	 */
	static embedded = ['tokens']
	
	/**
	 * Restricciones básicas de la clase
	 */

    static constraints = {
		
		nombre(blank:false)
		apellido(blank:false)
		nickname(blank:false,unique:true)
		password(minSize:4)
		email(email:true, blank:false,unique:true)
		fechaRegistro(blank:false)
		biografia(maxSize:200)
		pais(blank:false)
    }

	/**
	 * Persistencia almacenada en una base de datos MongoDB
	 */
	
	static mapWith = "mongo"
}
