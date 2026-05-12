package br.edu.ufape.agrofeira.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

data class ViaCepResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null,
)

@Service
class ViaCepService {
    private val restTemplate = RestTemplate()

    fun consultarCep(cep: String): ViaCepResponse? {
        val url = "https://viacep.com.br/ws/$cep/json"
        return try {
            val response = restTemplate.getForObject(url, ViaCepResponse::class.java)
            if (response?.erro == true) null else response
        } catch (e: Exception) {
            null
        }
    }
}
