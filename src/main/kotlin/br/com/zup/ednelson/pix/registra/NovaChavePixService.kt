package br.com.zup.ednelson.pix.registra

import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val chaveRepository: ChaveRepository) {

    fun registra(@Valid novaChave: NovaChavePixDto): Chave {

        if (chaveRepository.existsByValor(novaChave.valor)) {
            throw ChavePixExistenteException()
        }

        val chave = novaChave.toModel(novaChave)
        chaveRepository.save(chave)
        return chave
    }

}
