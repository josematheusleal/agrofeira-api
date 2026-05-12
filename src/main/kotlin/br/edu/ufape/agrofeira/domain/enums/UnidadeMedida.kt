package br.edu.ufape.agrofeira.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Unidades de medida permitidas para produtos")
enum class UnidadeMedida(
    val descricao: String,
) {
    QUILO("Quilograma (kg)"),
    GRAMA("Grama (g)"),
    LITRO("Litro (l)"),
    MILILITRO("Mililitro (ml)"),
    UNIDADE("Unidade (un)"),
    DUZIA("Dúzia (dz)"),
    CAIXA("Caixa (cx)"),
    PACOTE("Pacote (pct)"),
    SACO("Saco (sc)"),
    PORCAO("Porção (pc)"),
    METRO("Metro (m)"),
    CENTIMETRO("Centímetro (cm)"),
    FATIA("Fração (f)"),
}
