package miorquidea
import java.util.List;

import org.bson.types.ObjectId
/**
*
* @author Sara 
* Clase destinada a gestionar todas las funcionalidades del token de los usuarios
*
*/

class Token { 

	ObjectId id
	Date fechaCreacion
	boolean validez
	String ip
	String host
	static constraints = {
	}
	
	static mapping = {
		id index:true
	}

	/**
	* Persistencia almacenada en una base de datos MongoDB
	*/
   
   static mapWith = "mongo"
   
   public static boolean unTokenPorIp(Usuario usuario, String ip)
   {
	   
	   if(usuario!=null && usuario.tokens!=null)
	   {
		   vigenciaToken(usuario,ip)
		   
		   for(Token unToken : usuario.tokens)
		   {
			   if(unToken.validez== true && unToken.ip == ip)
			   {
				   return false
			   }
		   }
		   return true
	   }
	   return false
   }
   
   public static boolean tokenValido(Usuario usuario, String ip)
   {
	   
	   if(usuario!=null && usuario.tokens!=null)
	   {
		   vigenciaToken(usuario,ip)
		   
		   for(Token unToken : usuario.tokens)
		   {
			   if(unToken.validez== true && unToken.ip == ip)
			   {
				   return true
			   }
		   }
		   return false
	   }
	   return false
   }
   
 public static void anularToken(Usuario usuario, String ip)
 {
	 for(Token unToken : usuario.tokens)
	 {
		 if(unToken.validez== true && unToken.ip == ip)
		 {
			 unToken.validez = false
			 usuario.save()
			 break
		 }
	 }
 }
 
 public static void vigenciaToken(Usuario usuario, String ip)
 {
	 for(Token unToken : usuario.tokens)
	 {
		 if(unToken.validez== true && unToken.ip == ip)
		 {
			 int minutoActual = new Date().getCalendarDate().minutes
			 
			 if ( minutoActual < unToken.fechaCreacion.getCalendarDate().minutes)
			 {
				 minutoActual = minutoActual + 60
			 }
			 
			 if( Math.abs(new Date().getCalendarDate().hours - unToken.fechaCreacion.getCalendarDate().hours) > 1 ||
			 (Math.abs(minutoActual - unToken.fechaCreacion.getCalendarDate().minutes) > 4))
			 {
			 unToken.validez = false
			 usuario.save()
			
			 }
			 break
		 }
	 }
 }
 
}
