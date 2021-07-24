package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePixDto(
    @field:NotBlank val clienteId: String?,
    @field:Size(max = 77) val chave: String?,
    @field:NotNull val tipoChave: TipoChave?,
    @field:NotNull val tipoConta: TipoConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            tipoChave = TipoChave.valueOf(this.tipoChave!!.name),
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }

}