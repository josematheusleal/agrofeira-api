package br.edu.ufape.agrofeira.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Categorias permitidas para produtos")
enum class CategoriaProduto(
    val descricao: String,
) {
    HORTIFRUTI("Hortifrúti"),
    LATICINIOS("Laticínios"),
    ARTESANATO("Artesanato"),
    PROCESSADOS("Processados"),
    FRUTAS("Frutas"),
    OUTROS("Outros"),
}
