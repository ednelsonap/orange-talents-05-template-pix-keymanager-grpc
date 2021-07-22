package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta
import java.util.*
import javax.persistence.*

@Entity
class Chave(
    val clienteId: String?,
    val valor: String?,
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    val pixId = UUID.randomUUID().toString()
}
