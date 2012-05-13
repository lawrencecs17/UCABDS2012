package miorquidea

import grails.converters.XML
import org.springframework.web.context.request.RequestContextHolder


 
/**
 * Clase controlador encargada de gestionar los servicios tokens de usuarios
 * del sistema
 * @author Sara
 *
 */

class TokenController {	

    def index = {
			redirect(action:"list")				
		 }
	
	/**
	 * Metodo encargado del servicio de listar todos los tokens
	 * registrados en el sistema
	 * Debe ser solicitado mediante una peticion GET
	 */
	
	def list = {
		
		if(request.method !="GET")
		{			
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			if(Token.list())
			{
				render Token.list() as XML
			}
			else
			{				
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",fecha:new Date(),datos: false) as XML
			}
		}
	}
	/**
	 * Servicio de inicio de sesion de usuarios
	 * Debe ser solicitado mediante una peticion POST
	 */
	def iniciarSesion = {
		
		def usuario = null
		
		if(request.method !="POST")
		{
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{			
			asignarToken(procesarXML())
		
		}
	}
	/**
	 * Metodo que realiza la busqueda de un usuario con el 
	 * email y password obtenidos del XML
	 */
	def procesarXML()
	{
		try
		{
			def usuario = null
			def xml = request.XML
			usuario = Usuario.findByEmailAndPassword(xml.email,xml.password)		
			return usuario
			
		}
		catch(Exception)
		{
			render new RespuestaServidor(mensaje:"Se ha producido una Excepcion al procesar los datos de entrada del cliente",fecha:new Date(),datos: false) as XML
		}
	}
	/**
	 * Asignacion del token
	 */
	def asignarToken(Usuario usuario)
	{
		if(usuario)
		{
			if(usuario.activo)
			{
				if(validarToken(usuario))
				{						
					usuario.tokens << new Token(fechaCreacion:new Date(),validez: true,usuario:usuario,ip:request.getRemoteAddr(), host: request.getRemoteHost())		
					usuario.save()
					render usuario.tokens as XML
				}
				else
				{
					render new RespuestaServidor(mensaje:"El usuario ya tiene un token valido asignado en esta ubicacion "+request.getRemoteAddr(), fecha: new Date(), datos:false) as XML
				}
			}
			else
			{
				render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" ha desactivado su cuenta", fecha: new Date(), datos:false) as XML				
			}
			
		}
		else
		{
			render new RespuestaServidor(mensaje:"Login y/o Password invalidos",fecha:new Date(),datos: false) as XML
		}
	}
	
	boolean validarToken(Usuario usuario)
	{
		try
		{
			//Usuario user = Usuario.collection.find('email': usuario.email)[0]		
			
			if(usuario)
			{
				//List<Token> misTokens = usuario.tokens	
								
				return Token.unTokenPorIp(usuario, request.getRemoteAddr())				
				
			}
			else
			{			
				render new RespuestaServidor(mensaje:"Email no registrado en el Sistema", fecha: new Date(), datos:false) as XML
			}
		}
		catch(Exception)
		{
			render new RespuestaServidor(mensaje:"Se ha generado una falla de acceso a datos en el sistema", fecha: new Date(), datos:false) as XML
		}	
	}
	
	def consultarMiToken()
	{	
		
		if(request.method !="POST")
		{
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			miToken(procesarXML())		
		}		
	
	}
	
	def miToken(Usuario usuario)
	{
		try
		{			
			if(usuario)
			{							
				render usuario.tokens as XML	
				
			}
			else
			{
				render new RespuestaServidor(mensaje:"Login y/o Password invalidos",fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
			render new RespuestaServidor(mensaje:"Se ha producido una Excepcion al procesar los datos de entrada del cliente",fecha:new Date(),datos: false) as XML
		}
	}
	
	def anularToken()
	{
		try
		{
			Usuario usuario = null
			def xml = request.XML
			usuario = Usuario.findByEmail(xml.email,xml.password)
			if(usuario)
			{
				if(usuario.tokens)
				{
					Token.anularToken(usuario, request.getRemoteAddr())	
					render usuario.tokens as XML
				}
				else
				{
					render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" no tiene tokens asignados",fecha:new Date(),datos: false) as XML
				}
			}
			else
			{
				render new RespuestaServidor(mensaje:"Login y/o Password invalidos",fecha:new Date(),datos: false) as XML
				
			}
		}
		catch(Exception)
		{
			render new RespuestaServidor(mensaje:"Se ha producido una Excepcion al procesar los datos de entrada del cliente",fecha:new Date(),datos: false) as XML			
		}
	}
	
	def vigenciaToken()
	{
		Usuario usuario = null
		def xml = request.XML
		usuario = Usuario.findByEmail(xml.email)
		Token.vigenciaToken(usuario, request.getRemoteAddr())
		render usuario.tokens as XML		
	}
	
	
}
