class UrlMappings {

	static mappings = {
		
		"/comentario/crearComentario" (controller: "comentario", action:"crearComentario")
		"/comentario/crearComentado" (controller: "comentario", action:"crearComentado")
		"/comentario/modificarComentario" (controller: "comentario", action:"modificarComentario")
		"/comentario/eliminarComentario" (controller: "comentario", action:"eliminarComentario")
		"/comentario/listarPorUsuario" (controller: "comentario", action:"listarPorUsuario")
		"/comentario/listarPorComentario" (controller: "comentario", action:"listarPorComentario")
		"/comentario/listarPorEtiqueta" (controller: "comentario", action:"listarPorEtiqueta")
		"/comentario/listarSinEtiqueta" (controller: "comentario", action:"listarSinEtiqueta")
		"/comentario/listarTodos" (controller: "comentario", action:"listarTodos")
		"/comentario/contarComentados" (controller: "comentario", action:"contarComentados")
		
		"/usuario/registrarUsuario" (controller: "usuario", action:"registrarUsuario")
			
		"/etiqueta/crearEtiqueta" (controller: "etiqueta", action:"crearEtiqueta")
		"/etiqueta/listarTodos" (controller: "etiqueta", action:"listarTodos")
		
		"/calificacion/crearCalificacion" (controller: "calificacion", action:"crearCalificacion")
		"/calificacion/modificarCalificacion" (controller: "calificacion", action:"modificarCalificacion")
		"/calificacion/listarTodos" (controller: "calificacion", action:"listarTodos")
		"/calificacion/listarPorComentario" (controller: "calificacion", action:"listarPorComentario")
		"/calificacion/listarPorUsuario" (controller: "calificacion", action:"listarPorUsuario")
		"/calificacion/listarPorUsuarioComentario" (controller: "calificacion", action:"listarPorUsuarioComentario")
		"/calificacion/consultarLikeDislile" (controller: "calificacion", action:"consultarLikeDislile")

		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}