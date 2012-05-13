
import miorquidea.Usuario

class BootStrap {

    def init = { servletContext ->
		
		Usuario.collection.ensureIndex("tokens.validez")
		
    }
    def destroy = {
    }
}
