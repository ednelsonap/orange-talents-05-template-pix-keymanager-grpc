package br.com.zup.ednelson.pix.registra

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String?): Boolean
    fun existsByChavePixId(chavePixId: String?): Boolean
    fun findByChavePixId(chavePixId: String?): Optional<ChavePix>

}
