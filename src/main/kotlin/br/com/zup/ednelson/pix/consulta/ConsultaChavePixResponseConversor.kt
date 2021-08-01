package br.com.zup.ednelson.pix.consulta

import br.com.zup.ednelson.ConsultaChavePixResponse
import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId


class ConsultaChavePixResponseConversor {

    fun converte(chaveInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId.toString())
            .setChavePixId(chaveInfo.chavePixId.toString())
            .setChave(ConsultaChavePixResponse.Chave
                .newBuilder()
                .setTipoChave(TipoChave.valueOf(chaveInfo.tipoChave))
                .setChave(chaveInfo.chave)
                .setConta(ConsultaChavePixResponse.Chave.ContaInfo.newBuilder()
                    .setTipoConta(TipoConta.valueOf(chaveInfo.tipoConta))
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })

            )
            .build()
    }
}

//outra forma de converter para o Timestamp do GRPC
/*
internal fun LocalDateTime.toGrpcTimestamp(): Timestamp {
    val instant = this.atZone(ZoneId.of("UTC")).toInstant()
    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}*/
