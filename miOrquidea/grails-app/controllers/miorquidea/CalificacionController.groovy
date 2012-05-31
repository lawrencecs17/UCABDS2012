package miorquidea

import grails.converters.XML
import org.apache.commons.logging.*

class CalificacionController {

	RespuestaServidor respuesta
	CalificacionServidor cantidadCalificacion
	private static Log log = LogFactory.getLog("Logs."+CalificacionController.class.getName())
	 
	def index ={
		log.info "Entre en el index()"
		redirect(action: "listarTodos")
	}

	/**
	* Metodo que lista todos las calificaciones registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
	def listarTodos ={
				
		if(request.method !="GET")
		{
			log.error ("Peticion no permitida " + request.method + " en listarTodos")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			if(Calificacion.list())
			{
				log.info ("Imprimiendo todas las Calificaciones")
				render Calificacion.list() as XML
			}
			else
			{
				log.info ("No hay recursos encontrados en listarTodos")
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	/**
	* Metodo que lista todos las calificaciones por comentario registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
	def listarPorComentario ={
				
		if(request.method !="GET")
		{
			log.error ("Peticion no permitida " + request.method + " en listarPorComentario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			try
			{
				def calificacion = Calificacion.findAllByComentario(Comentario.get(params.comentario))
				
				if(calificacion)
				{
					render calificacion as XML
				}
				else
				{
					log.info ("No se encuentra calificaciones con el comentario id=" + params.comentario)
					render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
				log.error ("No se encuentra el comentario id=" + params.comentario)
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	/**
	* Metodo que lista todos las calificaciones por usuario registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
	def listarPorUsuario ={
				
		if(request.method !="GET")
		{
			log.error ("Peticion no permitida " + request.method + " en listarPorUsuario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			try
			{
				def usuario = Usuario.findByNicknameAndActivo(params.usuario, true)
				if (usuario)
				{
					def calificacion = Calificacion.findAllByPersona(Usuario.get(usuario.id))
				
					if(calificacion)
					{
						render calificacion as XML
					}
					else
					{
						log.info ("No se encuentra calificaciones con el usuario '" + params.usuario + "'")
						render new RespuestaServidor(mensaje:"No hay recursos encontrados",
							fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.info ("No se encuentra el usuario '" + params.usuario + "'")
					render new RespuestaServidor(mensaje:"El Usuario no existe",
						fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
				log.error ("No se encuentra el usuario '" + params.usuario + "'")
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	/**
	* Metodo que lista todos las calificaciones por usuario y comentario
	* registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
	def listarPorUsuarioComentario ={
				
		if(request.method !="GET")
		{
			log.error ("Peticion no permitida " + request.method + " en listarPorUsuarioComentario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			try
			{
				def usuario = Usuario.findByNicknameAndActivo(params.usuario, true)
				if (usuario)
				{
					def calificacion = Calificacion.findAllByPersonaAndComentario(Usuario.get(usuario.id) , Comentario.get(params.comentario))
				
					if(calificacion)
					{
						render calificacion as XML
					}
					else
					{
						log.info ("No se encuentra calificaciones con el usuario '" + params.usuario + "' " +
							"y comentario id=" + params.comentario)
						render new RespuestaServidor(mensaje:"No hay recursos encontrados",
							fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.info ("El Usuario '" + params.usuario + "' no existe")
					render new RespuestaServidor(mensaje:"El Usuario no existe",
						fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
				log.error ("No hay recursos encontrados con el usuario '" + params.usuario + "' " +
							"y comentario id=" + params.comentario)
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			}
		}
	}
	
	/**
	* Metodo encargado de Registrar Calificaciones en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearCalificacion ={
		
		if(request.method != "POST")
		{
			log.error ("Peticion no permitida " + request.method + " en crearCalificacion")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			procesarXmlCalificacion()
		}
	}
	
	/**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de CALIFICACION
	*/
	def procesarXmlCalificacion ={
	   
	   try
	   {
		   def xml = request.XML
		   def usuario = Usuario.findByNicknameAndActivo(xml.persona.text(), true)
		   if (usuario)
		   {
				   if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
				{
					if(!Calificacion.findByPersonaAndComentario(Usuario.get(usuario.id) , Comentario.get(xml.comentario.@id.text())))
					{
						def calificacionInstance = new Calificacion(like: xml.like.text() , dislike: xml.dislike.text() )
						calificacionInstance.comentario = Comentario.get(xml.comentario.@id.text())
						calificacionInstance.persona = Usuario.get(usuario.id)
						
						validarCalificacion(calificacionInstance)
					}
					else
					{
						log.info ("El usuario '" + usuario.nickname + "' ya califico sobre el comentario id=" + xml.comentario.@id.text())
						   render new RespuestaServidor(mensaje:"Ud. ya califico este comentario",
							   fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.info ("El usuario '" + usuario.nickname + "' no se encuentra logueado")
					   render new RespuestaServidor(mensaje:"Usuario no Logueado",
						   fecha:new Date(),datos: false) as XML
				}
		   }
		   else
		   {
			   log.info ("El usuario '" + usuario.nickname + "' no existe")
			   render new RespuestaServidor(mensaje:"El Usuario no existe",
				   fecha:new Date(),datos: false) as XML
		   }
	   }
	   catch(Exception)
	   {
		   log.error ("Error en recepcion de archivo XML en el metodo crearCalificacion")
		   render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en el metodo crearCalificacion",
			   fecha:new Date(),datos: false) as XML
	   }
   }
   
   /**
   * Metodo que valida si la insercion en la base de datos
   * fue exitosa
   */
   def validarCalificacion(Calificacion calificacion)
   {
	   try
	   {
		   if (!validarCalificacionDatos(calificacion.like , calificacion.dislike))
		   {
				if (calificacion.save(flush: true))
				{
					render calificacion as XML
				}
				else
				{
					log.error ("Error en datos, datos duplicados o formato incorrecto de entrada de Insertar Calificacion")
					render new RespuestaServidor(mensaje:"Error en datos, datos duplicados o formato incorrecto de entrada",
					   fecha:new Date(),datos: false) as XML
				}
		   }
		   else
		   {
			   log.error ("Los campos Like y Dislike son iguales a TRUE en el comentario id=" + calificacion.comentario)
			   render new RespuestaServidor(mensaje:"Los campos Like y Dislike son iguales a TRUE",
					   fecha:new Date(),datos: false) as XML
		   }
	   }
	   catch(Exception)
	   {
		   log.error ("Error en insercion de datos de Calificacion")
		   render new RespuestaServidor(mensaje:"Error en insercion de datos de archivo XML validarCalificacion en persistencia",
			   fecha:new Date(),datos: false) as XML
	   }
   }
   
   /**
   * Metodo que valida que lo atributos Like y Dislike
   * no sean iguales a TRUE
   */
   def validarCalificacionDatos(boolean like , boolean dislike)
   {
	   if (like.equals(true) && dislike.equals(true))
		   return true
	   else
		   return false
   }
   
   /**
   * Metodo encargado de Modificar Calificaciones en Persistencia
   * Debe ser solicitado mediante una peticion PUT
   */
   def modificarCalificacion()
   {
	   if(request.method != "PUT")
	   {
		   log.error ("Peticion no permitida " + request.method + " en modificarCalificacion")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   verificarXmlModificar()
	   }
   }
   
   /**
   * Metodo encargado de transformar los atributos LIKE e DISLIKE
   * a booleanos
   */
   def booleano(String likedislike)
   {
		if (likedislike.equals('true'))
		{
			return true
		}
		else
		{
			if (likedislike.equals('false'))
			{
				return false
			}
		}
		return null
   }
   
   /**
   * Metodo que se encarga de extraer los datos contenidos en un XML
   * de CALIFICACION para modificar
   */
   def verificarXmlModificar = {
	   try
	   {
		   def xml = request.XML
		   def usuario = Usuario.findByNicknameAndActivo(xml.persona.text(), true)
		   if (usuario)
		   {
			   if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
			   {
				   def calificacion = Calificacion.findByComentarioAndPersona(Comentario.get(xml.comentario.@id.text()), Usuario.get(usuario.id))
			  
				   if(calificacion)
				   {
					   calificacion.like = booleano(xml.like.text())
					   calificacion.dislike = booleano(xml.dislike.text())
					   validarCalificacion(calificacion)
				   }
				   else
				   {
					   log.info ("No ya recursos encontrados con el comentario " + xml.comentario.@id.text() + " y usuario '" + usuario.nickname +"'")
					   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
				   }
			   }
			   else
			   {
				   log.info ("El usuario '" + usuario.nickname + "' no se encuentra logueado")
					   render new RespuestaServidor(mensaje:"Usuario no Logueado",
						   fecha:new Date(),datos: false) as XML
			   }
		   }
		   else
		   {
			   log.info ("El usuario '" + usuario.nickname + "' no existe")
				   render new RespuestaServidor(mensaje:"El Usuario no existe",
					   fecha:new Date(),datos: false) as XML
		   }
	   }
	   catch(Exception)
	   {
		   log.error ("Error en recepcion de archivo XML en el metodo verificarXmlModificar")
		   render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en verificarXmlModificar",
			   fecha:new Date(),datos: false) as XML
	   }
   }
   
	/**
	* Metodo que cuenta todos las calificaciones que tenga like y dislike
	* registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
   def consultarLikeDislile = {
	   if(request.method !="GET")
	   {
		   log.error ("Peticion no permitida " + request.method + " en consultarLikeDislile")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
					   fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   try
		   {
			   def like = Calificacion.findAllByComentarioAndLike(Comentario.get(params.idComentario), true)
			   def dislike = Calificacion.findAllByComentarioAndDislike(Comentario.get(params.idComentario), true)
			   def contador = 0
			   if (like)
			   {
				   contador ++
			   }
			   if (dislike)
			   {
				   contador ++
			   }
			   if(contador > 0)
			   {
				   def cantidadLike = 0
				   def cantidadDislike = 0
				   like.each {
					   cantidadLike ++
				   }
				   dislike.each {
					   cantidadDislike ++
				   }
				   render new CalificacionServidor(Like: cantidadLike , Dislike: cantidadDislike) as XML
				   /*render new RespuestaServidor(mensaje:"Este comentario tiene " + cantidadLike + " like y " +
						   cantidadDislike + " dislike",
					   fecha:new Date(),datos: false) as XML*/
			   }
			   else
			   {
				   log.debug ("El comentario id= " + params.idComentario + "no tiene calificaciones")
				   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						  fecha:new Date(),datos: false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   log.error ("No hay recursos encontrados ")
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						  fecha:new Date(),datos: false) as XML
		   }
	   }
   }

}
