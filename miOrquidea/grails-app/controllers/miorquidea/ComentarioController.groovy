package miorquidea

import grails.converters.XML
import org.springframework.dao.DataIntegrityViolationException
import miorquidea.Comentario
import grails.converters.XML
import org.apache.commons.logging.*
import org.springframework.dao.DataIntegrityViolationException
import miorquidea.Comentario
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

class ComentarioController {

	RespuestaServidor respuesta
	CalificacionServidor cantidadCalificacion
	private static Log log = LogFactory.getLog("Logs."+ComentarioController.class.getName())
	
	def index() {
		redirect(action: "listarTodos")
	}
	
	/**
	* Metodo encargado de Registrar Comentarios en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearComentario ={
		
		log.info ("crearComentario")
		if(request.method != "POST")
		{
			log.error ("Peticion no permitida " + request.method + " en crearComentario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			log.info ("crearComentario procesarXmlComentario()")
			procesarXmlComentario()
		}
	}
	
	/**
	* Metodo encargado de Registrar Comentarios comentados en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearComentado ={
		
		log.info ("crearComentado")
		if(request.method != "POST")
		{
			log.error ("Peticion no permitida " + request.method + " en crearComentado")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			log.info ("crearComentado procesarXmlComentado()")
			procesarXmlComentado()
		}
	}
	
	 /**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTARIO
	*/
	def procesarXmlComentario ={
		
		log.info ("procesarXmlComentario")
		try
		{
			def today= new Date()
			def xml = request.XML
			//println(xml.autor.text())
			Usuario usuario = Usuario.findByNicknameAndActivo(xml.autor.text(), true)
			println("entreeeee2134123123123")
			if (usuario)
			{
				//println("entreeeeefdfdfdfdfdfdf")
				log.info ("procesarXmlComentario usuario = " + xml.autor.text())
				if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
				{
					//println("entreeeee")
					if (xml.mensaje.text())
					{
						def comentarioInstance = new Comentario(mensaje: xml.mensaje.text() , fecha: today )
				
						xml.tag.each {
							def nombreEtiqueta = it.etiqueta.text()
							def etiqueta = Etiqueta.findByNombre(nombreEtiqueta.toLowerCase())
							if (etiqueta)
							{
								comentarioInstance.addToTag(Etiqueta.get(etiqueta.id))
							}
							else
							{
								def insertarEtiqueta = new Etiqueta(nombre : nombreEtiqueta.toLowerCase())
								insertarEtiqueta.save()
								comentarioInstance.addToTag(Etiqueta.get(insertarEtiqueta.id))
							}
						}
						comentarioInstance.autor = Usuario.get(usuario.id)
						comentarioInstance.principal = true
						validarComentario(comentarioInstance)
			
						render comentarioInstance as XML
					}
					else
					{
						log.info ("Debe escribir un mensaje")
						render new RespuestaServidor(mensaje:"Debe escribir un mensaje",
							fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.info ("El usuario '" + usuario.nickname + "' debe estar logueado")
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
			log.error ("Error en recepcion de archivo XML en el metodo procesarXmlComentario")
			render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en el metodo procesarXmlComentario",
					 fecha:new Date(),datos: false) as XML
		}
	}
	
	/**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTARIO comentado
	*/
	def procesarXmlComentado ={
		
		log.info ("procesarXmlComentado")
		try
		{
		   def xml = request.XML
		   def today= new Date()
		   def comentarioInstance = Comentario.get(xml.comentario.@id.text())
		   Usuario usuario = Usuario.findByNicknameAndActivo(xml.autorComentado.text(), true)
		   if (usuario)
		   {
			   log.info ("procesarXmlComentado autorComentario = " + xml.autorComentado.text())
			   if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
			   {
				   if (xml.mensaje.text())
				   {
					   if (comentarioInstance.principal == true && Usuario.get(usuario.id))
					   {
						   def comentadoInstance = new Comentario(mensaje: xml.mensaje.text() , fecha: today )
						   comentadoInstance.autor = Usuario.get(usuario.id)
						   comentadoInstance.principal == false
					   
						   validarComentario(comentadoInstance)
						   comentarioInstance.addToComentado(comentadoInstance)
					   
						   validarComentario(comentarioInstance)
				
						   def usuarioPrincipal = Usuario.get(comentarioInstance.autor.id)
						   def usuarioComentado = Usuario.get(comentadoInstance.autor.id)

						   enviarCorreo(usuarioPrincipal.email , usuarioComentado.nickname, comentarioInstance.mensaje)
				   
						   render comentadoInstance as XML
					   }
					   else
					   {
						   log.info ("El usuario '" + usuario.nickname + "'no puede comentar sobre sub-comentarios o es un comentario que no existe")
							   render new RespuestaServidor(mensaje:"No puede comentar sobre sub-comentarios o es un comentario que no existe",
							   fecha:new Date(),datos: false) as XML
					   }
				   }
				   else
				   {
					   log.info ("Debe escribir un mensaje")
						   render new RespuestaServidor(mensaje:"Debe escribir un mensaje",
							   fecha:new Date(),datos: false) as XML
				   }
			   }
			   else
			   {
				   log.info ("El usuario '" + usuario.nickname + "' debe estar logueado")
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
			log.error ("Error en recepcion de archivo XML en el metodo procesarXmlComentado")
			render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en el metodo procesarXML Comentado",
				fecha:new Date(),datos: false) as XML
		}
	}
	
	/**
	* Metodo que valida si la insercion en la base de datos
	* fue exitosa
	*/
	def validarComentario(Comentario comentario)
	{
		try
		{
			if (comentario.save(flush: true))
			{
				 response.status = 201 // La petición ha sido completada y ha resultado en la creación de un nuevo recurso
				 log.info ("comentario creado")
				 //render comentario as XML
			}
			else
			{
				log.error ("Error en datos, datos duplicados o formato incorrecto de entrada en validarComentario")
				 render new RespuestaServidor(mensaje:"Error en datos, datos duplicados o formato incorrecto de entrada",
					   fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
			log.error ("Error en insercion de datos de archivo XML validarComentario")
			render new RespuestaServidor(mensaje:"Error en insercion de datos de archivo XML validarComentario en persistencia",
				fecha:new Date(),datos: false) as XML
		}
	}
	
	/**
	* Metodo encargado de Modificar Comentario en Persistencia
	* Debe ser solicitado mediante una peticion PUT
	*/
	def modificarComentario()
	{
		log.info ("modificarComentario")
		if(request.method != "PUT")
		{
			log.error ("Peticion no permitida " + request.method + " en modificarComentario")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
		}
		else
		{
			log.info ("modificarComentario verificarXmlModificar")
			verificarXmlModificar()
		}
	}
	
	/**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTRIO para modificar
	*/
	def verificarXmlModificar = {
		
		log.info ("verificarXmlModificar")
		try
		{
			def xml = request.XML
			def comentario = Comentario.get(xml.idComentario.@id.text())
			def usuario = Usuario.findByNicknameAndActivo(xml.usuario.text(), true)
			if (usuario)
			{
				log.info ("verificarXmlModificar usuario = " + xml.usuario.text())
				if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
				{
					if(comentario && usuario)
					{
						if( xml.mensaje.text())
						{
							if( comentario.autor.id == usuario.id )
							{
								comentario.mensaje = xml.mensaje.text()
								validarComentario(comentario)
								render comentario as XML
							}
							else
							{
								log.info ("El comentario id=" + xml.idComentario.@id.text() + " le pertenece a otro usuario")
								render new RespuestaServidor(mensaje:"Este comentario pertenece a otro usuario",
									fecha:new Date(),datos: false) as XML
							}
						}
						else
						{
							log.info ("No puede hacer publicaciones en blanco en el comentario id=" + xml.idComentario.@id.text())
							render new RespuestaServidor(mensaje:"No puede hacer publicaciones en blanco",
								fecha:new Date(),datos: false) as XML
						}
					}
					else
					{
						log.info ("No hay recursos encontrados en el comentario id=" + xml.idComentario.@id.text() +
							" y usuario '" + usuario.nickname + "'")
						render new RespuestaServidor(mensaje:"No hay recursos encontrados",
							fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					log.info ("El usuario '" + usuario.nickname + "' debe estar logueado")
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
			log.error ("Error en insercion de datos de archivo XML verificarXmlModificar")
			render new RespuestaServidor(mensaje:"Error en insercion de datos de archivo XML verificarXmlModificar en persistencia",
				fecha:new Date(),datos: false) as XML
		}
	}
	
	/**
	* Metodo encargado eliminar uno o mas comentario
	* Debe ser accedido mediante una peticion "DELETE"
	*/
   
   def eliminarComentario()
   {
	   log.info ("eliminarComentario")
	   if(request.method !="DELETE")
	   {
		   log.error ("Peticion no permitida " + request.method + " en eliminarComentario")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   def usuario = Usuario.findByNicknameAndActivo(params.usuario, true)
		   def comentario = Comentario.get(params.idComentario)
		   def comentarioUsuarios = Comentario.findByAutorAndPrincipal(Usuario.get(usuario.id), true)
		   boolean eliminar
		   if(Token.tokenValido(Usuario.get(usuario.id), request.getRemoteAddr()))
		   {
			   if(comentario)
			   {
				   if (comentario.autor.id == usuario.id)
				   {
					   eliminarCalificacion(params.int('idComentario'))
					   eliminarComentarioCascada(comentario, params.int('idComentario'))
					   render comentario as XML
				   }
				   else
				   {
					   comentarioUsuarios.each {
						   for ( comentados in it.comentado)
						   {
							   if (comentados.id == params.int('idComentario'))
							   {
									   eliminar = true
									break
							   }
							   else
									eliminar = false
						   }
					   }
					   if (eliminar == true )
					   {
						   eliminarCalificacion(params.int('idComentario'))
						   comentario.delete()
						   render comentario as XML
					   }
					   else
					   {
						   log.info ("El comentario id=" + params.idComentario + " le pertenece a otro usuario")
							   render new RespuestaServidor(mensaje:"Este comentario no le pertenece",
								   fecha:new Date(),datos: false) as XML
					   }
				   }
			   }
			   else
			   {
				   log.info ("No hay recursos encontrados con el comentario id=" + params.idComentario)
				   render new RespuestaServidor(mensaje:"El Comentario no existe",
						   fecha:new Date(),datos: false) as XML
			   }
		   }
		   else
		   {
			   log.info ("El usuario '" + usuario.nickname + "' debe estar logueado")
				   render new RespuestaServidor(mensaje:"Usuario no Logueado",
					   fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo encargado eliminar en cascada el comentario principal
   */
   def eliminarComentarioCascada(Comentario comentario, int idComentario)
   {
	   log.info ("eliminarComentarioCascada")
	   try
	   {
		   comentario.each {
			   for ( comentados in it.comentado)
			   {
				   if (comentados.id == idComentario)
				   {
					 def eliminar = Comentario.get(comentados.id)
					 eliminar.delete()
				   }
			   }
		   }
		   comentario.delete()
	   }
	   catch(Exception)
	   {
		   log.error ("problema " + request.method + "cascada")
		   render  new RespuestaServidor(mensaje:"problema " + request.method + "cascada",
			   fecha: new Date(),datos:false) as XML
	   }
   }
   
   /**
   * Metodo encargado eliminar las calificaciones de los comentarios
   */
   def eliminarCalificacion(int idComentario)
   {
	   log.info ("eliminarCalificacion")
	   try
	   {
		   def calificacion = Calificacion.findAllByComentario(Comentario.get(idComentario))
		   
		   if (calificacion)
		   {
			   calificacion.each{
				   def eliminar = Calificacion.get(it.id)
				   eliminar.delete()
			   }
		   }
	   }
	   catch(Exception)
	   {
		   log.error ("problema " + request.method + "calificacion")
		   render  new RespuestaServidor(mensaje:"problema" + request.method + "calificacion",
			   fecha: new Date(),datos:false) as XML
	   }
   }
   
   /**
   * Metodo que lista todos los comentario por usuario registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarPorUsuario ={
			 
	   log.info ("listarPorUsuario")
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
				   log.info ("listarPorUsuario = " + params.usuario)
				   def comentario = Comentario.findAllByAutorAndPrincipal(Usuario.get(usuario.id), true)
				   if(comentario)
				   {
					   render comentario as XML
				   }
				   else
				   {
					   log.info ("No hay recursos encontrados con el usuario " + params.usuario)
					   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
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
			   log.error ("No hay recursos encontrados en listarPorUsuario")
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo que lista todos los comentario por idComentario registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarPorComentario ={
			   
	   log.info ("listarPorUsuario")
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
			   def comentario = Comentario.find(Comentario.get(params.idComentario))
			   if(comentario)
			   {
				   log.info ("listarPorComentario = " + params.idComentario)
				   render comentario as XML
			   }
			   else
			   {
				   log.info ("No hay recursos encontrados con el comentario id=" + params.idComentario)
				   render new RespuestaServidor(mensaje:"El comentario no existe",
						   fecha:new Date(),datos: false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   log.error ("No hay recursos encontrados en listarPorComentario")
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados2",
						   fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo que lista todos los comentario por Etiquetas registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarPorEtiqueta ={
			   
	   log.info ("listarPorEtiqueta")
	   if(request.method !="GET")
	   {
		   log.error ("Peticion no permitida " + request.method + " en listarPorEtiqueta")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   try
		   {
			  def etiqueta = Etiqueta.findByNombre(params.nombre)
			  def comentario = Comentario.findAll()
			  List<Comentario> listaComentario = []
			  comentario.each{
				   for (book in it.tag)
				   {
					   if ( book.nombre == etiqueta.nombre )
					   {
							listaComentario.add(Comentario.get(it.id))
					   }
				   }
			   }
			   
			   if(listaComentario)
			   {
				   log.info ("listarPorEtiqueta = " + params.nombre)
				   render listaComentario as XML
				   listaComentario.clear()
			   }
			   else
			   {
				   log.info ("No hay recursos encontrados en listarPorEtiqueta")
				   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   log.error ("La etiqueta no existe")
			   render new RespuestaServidor(mensaje:"La etiqueta no existe",
						   fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo que lista todos los comentario que no tengan Etiquetas registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarSinEtiqueta ={
			   
	   if(request.method !="GET")
	   {
		   log.error ("Peticion no permitida " + request.method + " en listarSinEtiqueta")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
						fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   try
		   {
			  def comentario = Comentario.findAll()
			  List<Comentario> listaComentario = []
			  comentario.each{

					   if (!it.tag && it.principal == true)
					   {
						   listaComentario.add(Comentario.get(it.id))
					   }
			   }
			   
			   if(listaComentario)
			   {
				   render listaComentario as XML
				   listaComentario.clear()
			   }
			   else
			   {
				   log.info ("No hay recursos encontrados en listarSinEtiqueta")
				   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			   }
		   }
		   catch(Exception)
		   {
			   log.error ("No hay recursos encontrados en listarSinEtiqueta")
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo que lista todos los comentario registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarTodos ={
			  
	   log.info ("listarTodos")
	   if(request.method !="GET")
	   {
		   log.error ("Peticion no permitida " + request.method + " en listarTodos")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
					   fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   if(Comentario.list())
		   {
			   log.info ("retornando todos los comentario principales")
			   render Comentario.findAllByPrincipal(true) as XML
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
   * Metodo que lista la cantida de comentados que tiene un comentario principal
   * Debe ser solicitado mediante una peticion GET
   */
   def contarComentados ={
			 
	   log.info ("contarComentados")
	   if(request.method !="GET")
	   {
		   log.error ("Peticion no permitida " + request.method + " en contarComentados")
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
					   fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   try
		   {
			   def comentario = Comentario.get(params.idComentario)
			   if(comentario.principal == true)
			   {
				   comentario.each {
				   
					   def cantidad = 0
					   for (comentados in it.comentado)
					   {
						   cantidad ++
					   }
					   render new CalificacionServidor(Comentados: cantidad) as XML
					   /*render new RespuestaServidor(mensaje:"Este comentario tiene " + cantidad + " comentados",
						   fecha:new Date(),datos: false) as XML*/
				   }
			   }
			   else
			   {
				   log.info ("Este comentario es un sub-comentario o el comentario no existe")
				   render new RespuestaServidor(mensaje:"Este comentario es un sub-comentario o el comentario no existe",
					   fecha:new Date(),datos: false) as XML
			   }

		   }
		   catch(Exception)
		   {
			   log.error ("No hay recursos encontrados en contarComentados")
			   render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						  fecha:new Date(),datos: false) as XML
		   }
	   }
   }
   
   /**
   * Metodo que enviar un correo al usuario principal del comentario notificando
   * que un usuario comento sobre su comentario
   */
   def enviarCorreo (String email, String nickname, String mensaje)
   {
	   log.info ("enviarCorreo")
	   try
	   {
		   sendMail{
			   to email
			   subject "Notificacion de Mi Orquidea"
			   body 'El usuario ' + nickname + ' comento su comentario : ' + mensaje
		  }
	   }
	   catch(Exception)
	   {
		   log.error ("Problemas al enviar al correo " + email)
	   }
   }
   
   def uploadFile ={
	   
	   log.info ("uploadFile")
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
			  if(params.email!=null && params.idcomentario!=null)
			  {
				  Usuario usuario = null
				  usuario = Usuario.findByEmail(params.email)
				  def comentario = Comentario.get(params.idcomentario)
				  
				  
				  if(usuario && comentario)
				  {
					  if(Token.tokenValido(usuario, request.getRemoteAddr()))
					  {
						  extraerArchivo(usuario, comentario)
						 
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
   
   def extraerArchivo(Usuario usuario, def comentario)
   {
	   log.info ("extraerArchivo")
	  try
	  {
			if(comentario.autor.id == usuario.id)
			{
				File miPath = new File("C:/miOrquidea/$usuario.nickname")
				String nombreArchivo = request.getFile(params.archivo).getOriginalFilename()
				miPath.mkdirs()
				def archivo = request.getFile(params.archivo)
				archivo.transferTo(new File("C:/miOrquidea/$usuario.nickname/$nombreArchivo"))
					  comentario.adjuntos.add(nombreArchivo)
				comentario.save()
				render  comentario.adjuntos as XML
			}
			else
			{
				log.error ("Solo puedes adjuntar archivos sobre tus comentarios")
				render  new RespuestaServidor(mensaje:"Error: Solo puedes adjuntar archivos sobre tus comentarios",fecha: new Date(),datos:false) as XML
			}
	  }
	  catch(Exception)
	  {
		  log.error ("Error al procesar archivo adjunto")
		  render  new RespuestaServidor(mensaje:"Error al procesar archivo adjunto",fecha: new Date(),datos:false) as XML
	  }
   }
   
}

