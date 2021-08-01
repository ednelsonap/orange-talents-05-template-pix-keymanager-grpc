package br.com.zup.ednelson.pix.consulta

import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.ChavePix
import br.com.zup.ednelson.pix.registra.ContaAssociada
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val chavePixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoChave: String,
    val chave: String,
    val tipoConta: String,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                chavePixId = chave.chavePixId,
                clienteId = chave.clienteId,
                tipoChave = chave.tipoChave.name,
                chave = chave.chave!!,
                tipoConta = chave.tipoConta.name,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}
