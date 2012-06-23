package miorquidea

import org.springframework.dao.DataIntegrityViolationException
import org.apache.commons.logging.*
import grails.converters.XML

class EtiquetaController {
 
	RespuestaServidor respuesta
	private static Log log = LogFactory.getLog("Logs."+EtiquetaController.class.getName())
	
	def index() {
		redirect(action: "listarTodos")
	}

    /**
	* Metodo que lista todos las etiquetas registrados en el sistema
	* Debe ser solicitado mediante una peticion GET
	*/
    def listarTodos ={
        		
		log.info ("listarTodos Etiqueta")
		if(request.method !="GET")
		{			
			log.error ("Peticion no permitida " + request.method + " en listarTodos")
			render  new RespuestaServidor(mensaje:"Tipo de peticion no permitida " + request.method,
				        fecha: new Date(),datos:false) as XML
		}
		else
		{
			if(Etiqueta.list())
			{		
				log.info ("retornando todas las Etiqueta")
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
