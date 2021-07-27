package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.remove.exception.ChaveNaoEncontradaException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository) {

    @Transactional
    fun remove(
        @NotBlank chavePixId: String,
        @NotBlank clienteId: String,
    ){
        val uuidChavePixId = UUID.fromString(chavePixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByChavePixIdAndClienteId(uuidChavePixId, uuidClienteId)
            .orElseThrow {ChaveNaoEncontradaException()}

        repository.delete(chave)
    }

}
