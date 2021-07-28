package br.com.zup.ednelson.pix.registra.clients

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: String
) {

}
