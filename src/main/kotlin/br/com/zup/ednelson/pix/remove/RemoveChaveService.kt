package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.remove.exception.ChaveNaoEncontradaException
import br.com.zup.ednelson.pix.remove.exception.DonoDaChaveDiferenteException
import io.micronaut.validation.Validated
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
        val chave = repository.findByChavePixId(chavePixId)
            .orElseThrow {ChaveNaoEncontradaException()}

        if (chave.clienteId.toString() != clienteId) throw DonoDaChaveDiferenteException()

        repository.delete(chave)
    }

}
