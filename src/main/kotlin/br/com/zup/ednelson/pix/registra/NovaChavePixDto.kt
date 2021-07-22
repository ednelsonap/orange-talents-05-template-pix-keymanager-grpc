package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePixDto(
    @field:NotBlank val clienteId: String?,
    @field:NotBlank @field:Size(max = 77) val valor: String?,
    @field:NotNull val tipoChave: TipoChave?,
    @field:NotNull val tipoConta: TipoConta?
) {

    fun toModel(novaChave: NovaChavePixDto): Chave {
        return Chave(
            clienteId,
            valor,
            TipoChave.valueOf(this.tipoChave!!.name),
            TipoConta.valueOf(this.tipoConta!!.name)
        )
    }

}