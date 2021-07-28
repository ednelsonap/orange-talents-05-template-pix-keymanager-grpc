package br.com.zup.ednelson.pix.registra.clients

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)
