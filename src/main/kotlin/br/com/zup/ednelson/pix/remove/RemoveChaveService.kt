package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.registra.clients.BcbClient
import br.com.zup.ednelson.pix.registra.clients.DeletePixKeyRequest
import br.com.zup.ednelson.pix.remove.exception.ChaveNaoEncontradaException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
) {

    @Transactional
    fun remove(
        @NotBlank chavePixId: String,
        @NotBlank clienteId: String,
    ){
        val uuidChavePixId = UUID.fromString(chavePixId)
        val uuidClienteId = UUID.fromString(clienteId)

        // 1 - Busca a chave pix no banco de dados local
        val chave = repository.findByChavePixIdAndClienteId(uuidChavePixId, uuidClienteId)
            .orElseThrow {ChaveNaoEncontradaException()}

        // 2 - Remove da banco de dados local
        repository.delete(chave)

        // 3 - Remove do Banco Central do Brasil
        val response = bcbClient.deleta(chave.chave!!, DeletePixKeyRequest(key = chave.chave!!, participant = chave.conta.ispb))
        if (response.status != HttpStatus.OK)
            throw IllegalStateException("Erro ao remover chave pix no Banco Central do Brasil (BCB)")

    }

}
