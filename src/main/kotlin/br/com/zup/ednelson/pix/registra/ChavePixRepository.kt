package br.com.zup.ednelson.pix.registra

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String?): Boolean
    fun existsByChavePixId(chavePixId: UUID): Boolean
    fun findByChavePixIdAndClienteId(chavePixId: UUID, clienteId: UUID): Optional<ChavePix>
    fun findByChavePixId(chavePixId: UUID?): Optional<ChavePix>
    fun findByChave(chave: String): Optional<ChavePix>
    fun findAllByClienteId(clienteId: UUID?): List<ChavePix>
}
