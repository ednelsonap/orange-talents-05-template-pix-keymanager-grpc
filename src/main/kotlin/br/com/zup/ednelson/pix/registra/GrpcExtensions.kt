package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.ChaveRequest
import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta

fun ChaveRequest?.toModel(): NovaChavePixDto {

    return NovaChavePixDto(
        clienteId = this?.clienteId,
        valor = this?.valor,
        tipoConta = if (TipoConta.valueOf(this?.tipoConta!!.name) == TipoConta.TIPO_CONTA_DESCONHECIDO) throw TipoDeContaDesconhecidoException() else this?.tipoConta,
        tipoChave = if (TipoChave.valueOf(this?.tipoChave!!.name) == TipoChave.TIPO_CHAVE_DESCONHECIDO) throw TipoDeChaveDesconhecidoException() else this?.tipoChave,
    )

}