package miorquidea

import grails.converters.XML
import org.apache.commons.logging.*

class UsuarioController {
	
	private static Log log = LogFactory.getLog("Logs."+CalificacionController.class.getName())

	def index ={
		
		redirect(action:"list")
		
		}
	
	/**
	 * Metodo que lista todos los usuarios registrados en el sistema
	 * Debe ser solicitado mediante una peticion GET
	 */
	
	def list = {
		if(request.method !="GET")
		{
			log.error ("Peticion no permitida " + request.method + " en list")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			if(Usuario.list())
			{
				render Usuario.list() as XML
			}
			else
			{
				log.error ("No hay recursos encontrados en list")
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	/**
	 * Metodo encargado de Registrar Usuarios en Persistencia
	 * Debe ser solicitado mediante una peticion POST
	 */
	
	def registrarUsuario = {
		
		if(request.method != "POST")
		{
			log.error ("Peticion no permitida " + request.method + " en registrarUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			def usuario = procesarXML()
			validarRegistro(usuario)
		}
	}
	
	/**
	 * Metodo que se encarga de extraer los datos contenidos en un XML
	 * de USUARIO
	 */
	
	def procesarXML()
	{
		try
		{
			def usuario = new Usuario()
			def xml = request.XML
			usuario.nombre = xml.nombre
			usuario.apellido = xml.apellido
			usuario.nickname = xml.nickname
			usuario.password = xml.password
			usuario.biografia = xml.biografia
			usuario.fechaRegistro = new Date()
			usuario.email = xml.email
			usuario.pais = xml.pais
			usuario.activo = true
			return usuario
		}
		catch(Exception)
		{
			log.error ("Error en recepcion de archivo XML RegistrarUsuario")
			render "Error en recepcion de archivo XML"
			return null
		}
	}
	
	/**
	 * Metodo que valida si la insercion en la base de datos
	 * fue exitosa
	 */
	
	def validarRegistro(Usuario usuario)
	{
		try
		{
			if(usuario.save(flush:true))
			{
				/**
				 * La petición ha sido completada y ha resultado en la creación de un nuevo recurso
				 */
				response.status = 201  
				render usuario as XML
			}
			else
			{
				/**
				 * La petición NO ha sido completada, por data duplicado o formato
				 * de los campos incorrecto
				 */
				response.status = 409
				log.error ("Error en datos de entrada, datos duplicados o formato incorrecto de entrada validarRegistro")				
				render new RespuestaServidor(mensaje:"Error en datos de entrada, datos duplicados o formato incorrecto de entrada",fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
			log.error ("Error en insercion de datos de archivo XML validarRegistro")
			render "Error en insercion de datos de archivo XML en persistencia"
		}
	}
	
	/**
	 * Metodo encargado de inactivar un usuario pasado su correo
	 * Debe ser accedido mediante una peticion "DELETE"
	 */
	
	def eliminarUsuario()
	{
		if(request.method !="DELETE")
		{
			log.error ("Peticion no permitida " + request.method + " en eliminarUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			Usuario usuario = Usuario.findByEmailAndPassword(params.email,params.password)
			if(usuario)
			{
				if(usuario.activo)
				{
					if(Token.tokenValido(usuario, request.getRemoteAddr()))
					{
						usuario.activo = false
						usuario.save()
						render usuario as XML
					}
					else
					{
						log.error ("El usuario '" + usuario.nickname + "' no ha iniciado sesion, imposible procesar solicitud")
						render new RespuestaServidor(mensaje:"No ha iniciado sesion, imposible procesar solicitud",fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.error ("El usuario '" + usuario.nickname + "' ya ha desactivado su cuenta")
					render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" ya ha desactivado su cuenta",fecha:new Date(),datos: false) as XML
				}
			}
			else
			{
				log.error ("El Login :" + usuario.nickname + " y/o Password :" + usuario.password + " son invalidos")
				render new RespuestaServidor(mensaje:"Login y/o password invalidos",fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	def activarUsuario()
	{
		if(request.method !="PUT")
		{
			log.error ("Peticion no permitida " + request.method + " en activarUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			def xml = request.XML
			Usuario usuario = Usuario.findByEmailAndPassword(xml.email,xml.password)
			if(usuario)
			{
				if(usuario.activo)
				{
					render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" ya tiene activa su cuenta",fecha:new Date(),datos: false) as XML
				}
				else
				{
					usuario.activo = true
					usuario.save()
					render usuario as XML
				}
			}
			else
			{
				log.error ("El Login :" + usuario.nickname + " y/o Password :" + usuario.password + " son invalidos")
				render new RespuestaServidor(mensaje:"Login y/o password invalidos",fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	def modificarUsuario()
	{
		if(request.method != "PUT")
		{
			log.error ("Peticion no permitida " + request.method + " en modificarUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			validarRegistro(verificarXML())
		}
	}
	
	def verificarXML()
	{
		try
		{		
			def xml = request.XML
			def usuario = Usuario.findByEmail(xml.email)
			if(usuario!=null)
			{
				if(usuario.activo==true)
				{
					if(Token.tokenValido(usuario, request.getRemoteAddr()))
					{
						usuario.nombre = xml.nombre
						usuario.apellido = xml.apellido
						usuario.nickname = xml.nickname
						usuario.password = xml.password
						usuario.biografia = xml.biografia
						usuario.email = xml.email2
						usuario.pais = xml.pais
						
						return usuario
					}
					else
					{
						log.error ("El usuario '" + usuario.nickname + "' no ha iniciado sesion, imposible procesar solicitud")
						render new RespuestaServidor(mensaje:"No ha iniciado sesion, imposible procesar solicitud",fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.error ("El usuario '" + usuario.nickname + "' ya ha desactivado su cuenta")
					render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" ha desactivado su cuenta.",fecha:new Date(),datos: false) as XML
				}
			}
			else
			{
				log.error ("No hay recursos encontrados en verificarXML")
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
			log.error ("Error en recepcion de archivo XML verificarXML")
			render "Error en recepcion de archivo XML"
			return null
		}
	}
	
}
