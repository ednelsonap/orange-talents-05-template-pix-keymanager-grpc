package br.com.zup.ednelson.pix.registra.clients

import br.com.zup.ednelson.pix.registra.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorIdClienteETipo(
        @PathVariable clienteId: String,
        @QueryValue tipo: String
    ): HttpResponse<DadosDaContaResponse>

}
