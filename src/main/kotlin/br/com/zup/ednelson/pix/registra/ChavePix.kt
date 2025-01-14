package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.consulta.exception.ChavePixNaoPertenceAoClienteException
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(

    @field:NotNull
    @Column(nullable = false)
    @Lob
    val clienteId: UUID,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String?,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada

){
    fun pertenceAo(clienteIdAsUuid: UUID?): Boolean {
        if(this.clienteId == clienteIdAsUuid) {
            return true
        } else {
            throw ChavePixNaoPertenceAoClienteException()
        }
    }

    val tipoPessoa = TipoPessoa.NATURAL_PERSON

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    @Lob
    val chavePixId = UUID.randomUUID()
    val criadaEm: LocalDateTime = LocalDateTime.now()
}
