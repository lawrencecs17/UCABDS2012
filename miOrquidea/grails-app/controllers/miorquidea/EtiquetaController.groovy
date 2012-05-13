package miorquidea

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.XML

class EtiquetaController {
 
	RespuestaServidor respuesta
	
	def index() {
		redirect(action: "listarTodos")
	}
	
	/**
	* Metodo encargado de Registrar Etiquetas en Persistencia
	* Debe ser solicitado mediante una peticion POST
	*/
	def crearEtiqueta ={
		
		if(request.method != "POST")
		{
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			procesarXmlEtiqueta()
		}
	}
	
	 /**
    * Metodo que se encarga de extraer los datos contenidos en un XML
    * de ETIQUETA
    */
	def procesarXmlEtiqueta ={
		try
		{
			def xml = request.XML 
			if(Token.tokenValido(Usuario.get(xml.idUsuario.text()), request.getRemoteAddr()))
			{
				if (xml.nombre.text())
				{
					def mensaje = xml.nombre.text()
					def etiquetaInstance = new Etiqueta(nombre: mensaje.toLowerCase() )
					validarEtiqueta(etiquetaInstance)
				}
				else 
					render new RespuestaServidor(mensaje:"Debe escribir un nombre", 
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
	def validarEtiqueta(Etiqueta etiqueta)
	{
		try
		{
			 if (etiqueta.save(flush: true))
			 {
				 render etiqueta as XML
			 }
			 else
			 {
				 render new RespuestaServidor(mensaje:"Error en datos, datos duplicados o formato incorrecto de entrada",
						   fecha:new Date(),datos: false) as XML
			 }
		}
		catch(Exception)
		{
			render new RespuestaServidor(mensaje:"Error en insercion de datos de archivo XML validarEtiqueta en persistencia",
				fecha:new Date(),datos: false) as XML
		}
	}

    /**
	* Metodo que lista todos las etiquetas registrados en el sistema
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
			if(Etiqueta.list())
			{			
				render Etiqueta.list() as XML
			}
			else
			{
				render new RespuestaServidor(mensaje:"No hay recursos encontrados",
						   fecha:new Date(),datos: false) as XML
			}
		}
    }

}
