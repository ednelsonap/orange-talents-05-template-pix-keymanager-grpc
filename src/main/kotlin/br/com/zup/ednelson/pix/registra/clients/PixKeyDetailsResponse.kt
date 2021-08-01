package br.com.zup.ednelson.pix.registra.clients

import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.consulta.ChavePixInfo
import br.com.zup.ednelson.pix.registra.ContaAssociada
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createAt: LocalDateTime,
) {
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoChave = keyType,
            chave = key,
            tipoConta = when (this.bankAccount.accountType) {
                "CACC" -> TipoConta.CONTA_CORRENTE.name
                "SVGS" -> TipoConta.CONTA_POUPANCA.name
                else -> throw  IllegalStateException("Algo inesperável ocorreu")
            },
            conta = ContaAssociada(
                instituicao = if (bankAccount.participant == "60701190") {
                    "ITAÚ UNIBANCO S.A."
                } else {
                    throw  IllegalStateException("Algo inesperável ocorreu")
                },
                ispb = bankAccount.participant,
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }

}
