package br.com.zup.ednelson.pix.registra.clients

data class CreatePixKeyRequest(
    val keyType: String,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
) {

}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
) {

}

data class Owner(
    val type: String,
    val name: String,
    val taxIdNumber: String,
) {

}