package miorquidea

import grails.converters.XML
import java.text.SimpleDateFormat
import org.apache.commons.logging.*

class UsuarioController {
	
	private static Log log = LogFactory.getLog("Logs2."+UsuarioController.class.getName())

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
	 * Consultar un usuario
	 */
	def consultaUnUsuario = {
		
		try
		{
			Usuario miUsuario = Usuario.findByEmailAndPassword(params.email,params.password)
			if(miUsuario)
			{
				log.info ("consultaUnUsuario con email = " + params.email + " y password " + params.password )
				render miUsuario as XML
			}
			else
			{
				log.error ("consultaUnUsuario: Login y/o password invalidos con email = " + params.email + " y password " + params.password )
				render  new RespuestaServidor(mensaje:"Login y/o password invalidos",fecha: new Date(),datos:false) as XML
				
			}
		}
		catch(Exception)
		{
			log.error ("consultaUnUsuario: Error en transmision de datos a miOrquidea App" )
			render  new RespuestaServidor(mensaje:"Error en transmision de datos a miOrquidea App",fecha: new Date(),datos:false) as XML
		}
		
	}
	
	/**
	 * Metodo encargado de Registrar Usuarios en Persistencia
	 * Debe ser solicitado mediante una peticion POST
	 */
	
	def registrarUsuario = {
		
		log.info ("registrarUsuario")
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
		log.info ("procesarXML")
		try
		{
			def usuario = new Usuario()
			def xml = request.XML
			String fecha = xml.fechaRegistro.text()
			
			usuario.nombre = xml.nombre
			usuario.apellido = xml.apellido
			usuario.nickname = xml.nickname
			usuario.password = xml.password
			usuario.biografia = xml.biografia
			usuario.fechaRegistro = Date.parse("yy-MM-dd", fecha)
			usuario.email = xml.email
			usuario.pais = xml.pais
			usuario.activo = true
			
			log.info ("nombre= "+ xml.nombre + " apellido= " + xml.apellido + " nickanem= " + xml.nickname + " password= " + xml.password + " biografia= " + xml.biografia + " fecha= " + Date.parse("yy-MM-dd", fecha) + " email= " + xml.email + " pais= " + xml.pais)
			
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
		log.info ("validarRegistro")
		try
		{
			
			if(usuario.save(flush:true))
			{
				/**
				 * La petici�n ha sido completada y ha resultado en la creaci�n de un nuevo recurso
				 */
				response.status = 201   
				log.info ("validarRegistro usuario creado")
				render usuario as XML
			}
			else
			{
				/**
				 * La petici�n NO ha sido completada, por data duplicado o formato
				 * de los campos incorrecto
				 */
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
			log.info ("eliminarUsuario")
			Usuario usuario = Usuario.findByEmailAndPassword(params.email,params.password)
			if(usuario)
			{
				if(usuario.activo)
				{
					if(Token.tokenValido(usuario, request.getRemoteAddr()))
					{
						usuario.activo = false
						usuario.save()
						log.info "El usuario '" + usuario.nickname + "' ya ha desactivado su cuenta"
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
					log.info "El usuario '" + usuario.nickname + "' ya ha desactivado su cuenta"
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
		log.info("activarUsuario")
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
					log.info("activarUsuario: El usuario "+usuario.email+" ya tiene activa su cuenta")
					render new RespuestaServidor(mensaje:"El usuario "+usuario.email+" ya tiene activa su cuenta",fecha:new Date(),datos: false) as XML
				}
				else
				{
					log.info("activarUsuario: se ha activo el usuario " +usuario.email)
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
		log.info("modificarUsuario")
		if(request.method != "PUT")
		{
			log.error ("Peticion no permitida " + request.method + " en modificarUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida",fecha: new Date(),datos:false) as XML
		}
		else
		{
			log.info("modificarUsuario: validarRegistro")
			validarRegistro(verificarXML())
		}
	}
	
	def verificarXML()
	{
		log.info("verificarXML")
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
						
						log.info ("nombre= "+ xml.nombre + " apellido= " + xml.apellido + " nickanem= " + xml.nickname + " password= " + xml.password + " biografia= " + xml.biografia + " email= " + xml.email2 + " pais= " + xml.pais)
						
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
	
	def uploadFile ={
		
		log.info("uploadfile")
		if(request.method != "POST")
		{
			log.error ("Peticion no permitida " + request.method + " en uploadFile")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
		   try
		   {
			   if(params.email!=null)
			   {
				   Usuario usuario = null
				   usuario = Usuario.findByEmail(params.email)	  
				   
				   
				   if(usuario)
				   {
					   if(Token.tokenValido(usuario, request.getRemoteAddr()))
					   {
						   extraerArchivo(usuario)
						  
					   }
					   else
					   {
						   log.info ("La sesion del usuario '" + usuario.nickname+ "'" + " a expirado")
						   render  new RespuestaServidor(mensaje:"Su sesion a expirado, debe iniciar sesion para subir el archivo",fecha: new Date(),datos:false) as XML
					   }
					  
				   }
				   else
				   {
					   log.info ("Recurso no encontrado no se puede adjuntar archivo")
					  render  new RespuestaServidor(mensaje:"Recurso no encontrado no se puede adjuntar archivo ",fecha: new Date(),datos:false) as XML
				   }
				   
			   }
			   else
			   {
				   log.info ("Datos entradas incompletos")
				   render  new RespuestaServidor(mensaje:"Datos entradas incompletos ",fecha: new Date(),datos:false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   log.error ("Ocurrio un error al procesar los datos de entrada")
			   render  new RespuestaServidor(mensaje:"Ocurrio un error al procesar los datos de entrada ",fecha: new Date(),datos:false) as XML
		   }
		}
	}
	def extraerArchivo(Usuario usuario)
	{
	   try
	   { 
				 File miPath = new File("$params.path")
				 String nombreArchivo = request.getFile(params.archivo).getOriginalFilename()
				 miPath.mkdirs()
				 def archivo = request.getFile(params.archivo)
				 archivo.transferTo(new File("$params.path/${usuario.nickname}.png"))
				 render miPath.absolutePath					  
				 
			
	   }
	   catch(Exception)
	   {
		   log.error ("Error al procesar archivo adjunto")
		   render  new RespuestaServidor(mensaje:"Error al procesar archivo adjunto",fecha: new Date(),datos:false) as XML
	   }
	}
	
}
