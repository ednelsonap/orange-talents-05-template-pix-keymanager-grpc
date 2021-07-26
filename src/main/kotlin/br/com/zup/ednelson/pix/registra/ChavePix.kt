package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoConta
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(

    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID?,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    val chave: String?,

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    val chavePixId = UUID.randomUUID().toString()
}
