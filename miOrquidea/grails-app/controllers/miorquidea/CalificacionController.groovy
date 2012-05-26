package miorquidea

import grails.converters.XML

class CalificacionController {

    RespuestaServidor respuesta
	 
    def index ={
		
        redirect(action: "listarTodos")
    }

	/**
	* Metodo que lista todos las calificaciones registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	* ........
	*/
    def listarTodos ={
        		
		if(request.method !="GET")
		{			
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			if(Calificacion.list())
			{			
				render Calificacion.list() as XML
			}
			else
			{
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
					render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
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
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			try
			{
				def calificacion = Calificacion.findAllByPersona(Usuario.get(params.usuario))
				
				if(calificacion)
				{
					render calificacion as XML
				}
				else
				{
					render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
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
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			try
			{
				def calificacion = Calificacion.findAllByPersonaAndComentario(Usuario.get(params.usuario) , Comentario.get(params.comentario))
				
				if(calificacion)
				{
					render calificacion as XML
				}
				else
				{
					render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
				}
			}
			catch(Exception)
			{
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
		   if(Token.tokenValido(Usuario.get(xml.persona.@id.text()), request.getRemoteAddr()))
		   {
			   if(!Calificacion.findByPersonaAndComentario(Usuario.get(xml.persona.@id.text()) , Comentario.get(xml.comentario.@id.text())))
			   {
				   def calificacionInstance = new Calificacion(like: xml.like.text() , dislike: xml.dislike.text() )
				   calificacionInstance.comentario = Comentario.get(xml.comentario.@id.text())
				   calificacionInstance.persona = Usuario.get(xml.persona.@id.text())
						
				   validarCalificacion(calificacionInstance)
			   }
			   else
			   		render new RespuestaServidor(mensaje:"Ud. ya califico este comentario",
				        fecha:new Date(),datos: false) as XML
		   }
		   else
		   		render new RespuestaServidor(mensaje:"Usuario no Logueado",
					   fecha:new Date(),datos: false) as XML
	   }
	   catch(Exception)
	   {
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
					render new RespuestaServidor(mensaje:"Error en datos, datos duplicados o formato incorrecto de entrada",
				       fecha:new Date(),datos: false) as XML
				}	
		   }
		   else
		   {	   
			   render new RespuestaServidor(mensaje:"Los campos Like y Dislike son iguales a TRUE",
				       fecha:new Date(),datos: false) as XML  
		   }			   
	   }
	   catch(Exception)
	   {
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
		   if(Token.tokenValido(Usuario.get(xml.persona.@id.text()), request.getRemoteAddr()))
		   {
			   def calificacion = Calificacion.findByComentarioAndPersona(Comentario.get(xml.comentario.@id.text()), Usuario.get(xml.persona.@id.text()))
			  
			   if(calificacion)
			   {				
				   calificacion.like = booleano(xml.like.text())
				   calificacion.dislike = booleano(xml.dislike.text())
				   validarCalificacion(calificacion)
			   }
			   else
			   {
			    	render new RespuestaServidor(mensaje:"No hay recursos encontrados",
			    		fecha:new Date(),datos: false) as XML
			   }
		   }
		   else
		   		render new RespuestaServidor(mensaje:"Usuario no Logueado",
					   fecha:new Date(),datos: false) as XML
	   }
	   catch(Exception)
	   {
		   render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en verificarXML",
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
				   render new RespuestaServidor(mensaje:"Este comentario tiene " + cantidadLike + " like y " +
					       cantidadDislikecantidadDislike + " dislike",
					   fecha:new Date(),datos: false) as XML
			   }
			   else
			   {
				   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						  fecha:new Date(),datos: false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						  fecha:new Date(),datos: false) as XML
		   }
	   }
   }

}
