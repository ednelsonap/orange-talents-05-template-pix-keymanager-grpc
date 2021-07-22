package br.com.zup.ednelson.pix.registra

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChaveRepository : JpaRepository<Chave, Long> {

    fun existsByValor(valor: String?): Boolean

}
