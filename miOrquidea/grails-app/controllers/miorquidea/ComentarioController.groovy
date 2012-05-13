package miorquidea

import grails.converters.XML
import org.springframework.dao.DataIntegrityViolationException
import miorquidea.Comentario
import grails.converters.XML
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
import org.apache.log4j.Logger;

class ComentarioController { 

	RespuestaServidor respuesta
	
	def index() {
		redirect(action: "listarTodos")
	}
	
	/**
	* Metodo encargado de Registrar Comentarios en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearComentario ={
		
		if(request.method != "POST")
		{
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			procesarXmlComentario()
		}
	}
	
	/**
	* Metodo encargado de Registrar Comentarios comentados en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearComentado ={
		
		if(request.method != "POST")
		{
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			procesarXmlComentado()
		}
	}
	
	 /**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTARIO
	*/
	def procesarXmlComentario ={
		try
		{
			def today= new Date()
			def xml = request.XML
			
			if(Token.tokenValido(Usuario.get(xml.autor.@id.text()), request.getRemoteAddr()))
			{		
				if (xml.mensaje.text())
				{
					def comentarioInstance = new Comentario(mensaje: xml.mensaje.text() , fecha: today )			
				
					xml.tag.each {
						comentarioInstance.addToTag(Etiqueta.get(it.etiqueta.@id.text()))
					}				
					comentarioInstance.autor = Usuario.get(xml.autor.@id.text())
					comentarioInstance.principal = true
					validarComentario(comentarioInstance)	
			
					render comentarioInstance as XML
				}
				else
					render new RespuestaServidor(mensaje:"Debe escribir un mensaje",
						fecha:new Date(),datos: false) as XML
			}
			else
				render new RespuestaServidor(mensaje:"Usuario no Logueado",
						fecha:new Date(),datos: false) as XML
		}
		catch(Exception) 
		{
			render new RespuestaServidor(mensaje:"Error en recepcion de archivo XML en el metodo procesarXmlComentario",
				     fecha:new Date(),datos: false) as XML
		}
	}
	
	/**
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTARIO comentado
	*/
	def procesarXmlComentado ={
		try
		{
		   def xml = request.XML
		   def today= new Date()		
		   def comentarioInstance = Comentario.get(xml.comentario.@id.text())
		   
		   if(Token.tokenValido(Usuario.get(xml.autorComentado.@id.text()), request.getRemoteAddr()))
		   {
			   if (xml.mensaje.text())
			   {
				   if (comentarioInstance.principal == true && Usuario.get(xml.autorComentado.@id.text()))
				   {
					  
					   def comentadoInstance = new Comentario(mensaje: xml.mensaje.text() , fecha: today )
					   comentadoInstance.autor = Usuario.get(xml.autorComentado.@id.text())
					   comentadoInstance.principal == false
				   
					   
					   validarComentario(comentadoInstance)
					   comentarioInstance.addToComentado(comentadoInstance) 
					   
					   validarComentario(comentarioInstance)
				
					   def usuarioPrincipal = Usuario.get(comentarioInstance.autor.id)
					   def usuarioComentado = Usuario.get(comentadoInstance.autor.id)

					   enviarCorreo(usuarioPrincipal.email , usuarioComentado.nickname, comentarioInstance.mensaje)
				   
					   render comentarioInstance as XML  
					   println "aqui"
				   }
				   else
			   			render new RespuestaServidor(mensaje:"No puede comentar sobre sub-comentarios o es un comentario que no existe",
							   fecha:new Date(),datos: false) as XML
			   }
			   else
		   			render new RespuestaServidor(mensaje:"Debe escribir un mensaje",
					   fecha:new Date(),datos: false) as XML
		   }
		   else
		   		render new RespuestaServidor(mensaje:"Usuario no Logueado",
				   fecha:new Date(),datos: false) as XML
		}
		catch(Exception)
		{			
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
				 //render comentario as XML
			}
			else
			{
				 render new RespuestaServidor(mensaje:"Error en datos, datos duplicados o formato incorrecto de entrada",
				       fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
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
	* Metodo que se encarga de extraer los datos contenidos en un XML
	* de COMENTRIO para modificar
	*/
	def verificarXmlModificar = {
		try
		{
			def xml = request.XML
			def comentario = Comentario.get(xml.idComentario.@id.text())
			def usuario = Usuario.get(xml.idUsuario.@id.text())
			def idUSuario = Integer.parseInt(xml.idUsuario.@id.text())
			
			if(comentario && usuario)			
			{
				if( xml.mensaje.text())
				{
					if( comentario.autor.id == idUSuario )
					{
						comentario.mensaje = xml.mensaje.text()
						validarComentario(comentario)
						render comentario as XML
					}
					else
					{
						 render new RespuestaServidor(mensaje:"Este comentario pertenece a otro usuario",
							 fecha:new Date(),datos: false) as XML
					}
				}
				else
				{
					render new RespuestaServidor(mensaje:"No puede hacer publicaciones en blanco",
						fecha:new Date(),datos: false) as XML
				}
			}
			else
			{
				 render new RespuestaServidor(mensaje:"No hay recursos encontrados",
					 fecha:new Date(),datos: false) as XML
			}
		}
		catch(Exception)
		{
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
	   if(request.method !="DELETE")
	   {
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   def comentario = Comentario.get(params.idComentario)
		   //def usuario =  Usuario.get(params.idUsuario)
		   def comentarioUsuarios = Comentario.findByAutorAndPrincipal(Usuario.get(params.idUsuario), true)
		  // render comentario as XML
		   boolean eliminar
		   if(Token.tokenValido(Usuario.get(params.idUsuario), request.getRemoteAddr()))
		   {
			   if(comentario)
			   {
				   if (comentario.autor.id == params.int('idUsuario'))
				   {
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
						   comentario.delete()
						   render comentario as XML
					   }
					   else
					   		render new RespuestaServidor(mensaje:"Este comentario no le pertenece",
								   fecha:new Date(),datos: false) as XML
				   }
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
   }
   
   /**
   * Metodo encargado eliminar en cascada el comentario principal
   */
   def eliminarComentarioCascada(Comentario comentario, int idComentario)
   {
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
		   render  new RespuestaServidor(mensaje:"problema" + request.method,
			   fecha: new Date(),datos:false) as XML
	   }
   }
   
   /**
   * Metodo que lista todos los comentario por usuario registrados en el sistema
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
			   def comentario = Comentario.findAllByAutor(Usuario.get(params.idUsuario))
			   if(comentario)
			   {
				   render comentario as XML
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
   * Metodo que lista todos los comentario por idComentario registrados en el sistema
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
			   def comentario = Comentario.find(Comentario.get(params.idComentario))
			   if(comentario)
			   {
				   render comentario as XML
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
   * Metodo que lista todos los comentario por Etiquetas registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarPorEtiqueta ={
			   
	   if(request.method !="GET")
	   {
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   try
		   {		
			  def etiqueta = Etiqueta.get(params.idEtiqueta) 
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
				   render listaComentario as XML
			       listaComentario.clear()
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
   * Metodo que lista todos los comentario que no tengan Etiquetas registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarSinEtiqueta ={
			   
	   if(request.method !="GET")
	   {
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

					   if (!it.tag)
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
   * Metodo que lista todos los comentario registrados en el sistema
   * Debe ser solicitado mediante una peticion GET
   */
   def listarTodos ={
			   
	   if(request.method !="GET")
	   {
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
					   fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		   if(Comentario.list())
		   {
			   render Comentario.list() as XML
		   }
		   else
		   {
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
			   
	   if(request.method !="GET")
	   {
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
					   render new RespuestaServidor(mensaje:"Este comentario tiene " + cantidad + " comentados",
						   fecha:new Date(),datos: false) as XML
				   }
			   }
			   else
			   {
				   render new RespuestaServidor(mensaje:"Este comentario es un sub-comentario o el comentario no existe",
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
   * Metodo que enviar un correo al usuario principal del comentario notificando
   * que un usuario comento sobre su comentario
   */
   def enviarCorreo (String email, String nickname, String mensaje)
   {
	  sendMail{
		  to email
		  subject "Notificacion de Mi Orquidea"
		  body 'El usuario ' + nickname + ' comento su comentario : ' + mensaje
		  } 
   }
   
   def uploadFile ={
	   
	   if(request.method != "POST")
	   {
		   render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
					   fecha: new Date(),datos:false) as XML
	   }
	   else
	   {
		  /*try
		  {*/
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
						  render  new RespuestaServidor(mensaje:"Su sesion a expirado, debe iniciar sesion para subir el archivo",fecha: new Date(),datos:false) as XML
					  }
					 
				  }
				  else
				  {
					 render  new RespuestaServidor(mensaje:"Recurso no encontrado no se puede adjuntar archivo ",fecha: new Date(),datos:false) as XML
				  }
				  
			  }
			  else
			  {
				  render  new RespuestaServidor(mensaje:"Datos entradas incompletos ",fecha: new Date(),datos:false) as XML
			  }
		  //}
		  /*catch(Exception)
		  {
			  render  new RespuestaServidor(mensaje:"Ocurrio un error al procesar los datos de entrada ",fecha: new Date(),datos:false) as XML
		  }*/
	   }
   }
   
   def extraerArchivo(Usuario usuario, def comentario)
   {
	  try
	  {
				  if(comentario.autor.id == usuario.id)
			   {
					File miPath = new File("C:/miOrquidea/$usuario.nickname")
					String nombreArchivo = request.getFile(params.archivo).getOriginalFilename()
					miPath.mkdirs()
					def archivo = request.getFile(params.archivo)
					archivo.transferTo(new File("C:/Users/Lawrence/Desktop/miOrquidea/$usuario.nickname/$nombreArchivo"))
					   comentario.adjuntos.add(nombreArchivo)
					comentario.save()
					render  comentario.adjuntos as XML
			   }
			   else
			   {
				   render  new RespuestaServidor(mensaje:"Error: Solo puedes adjuntar archivos sobre tus comentarios",fecha: new Date(),datos:false) as XML
			   }
		   
	  }
	  catch(Exception)
	  {
		  render  new RespuestaServidor(mensaje:"Error al procesar archivo adjunto",fecha: new Date(),datos:false) as XML
	  }
	   
   }
}

