package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.ChavePixServiceGrpc
import br.com.zup.ednelson.ChaveRequest
import br.com.zup.ednelson.ChaveResponse
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.compartilhado.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class NovaChavePixGrpcEndpoint(
    @Inject val service: NovaChavePixService,
) : ChavePixServiceGrpc.ChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(NovaChavePixGrpcEndpoint::class.java)

    override fun cadastrar(request: ChaveRequest?, responseObserver: StreamObserver<ChaveResponse>?) {

        logger.info("Iniciando o cadastro da chave: $request")

        val novaChave = request!!.toModel()
        val chaveCriada = service.registra(novaChave)

        responseObserver?.onNext(ChaveResponse.newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setChavePixId(chaveCriada.chavePixId.toString())
            .build())
        responseObserver?.onCompleted()

    }
}

fun ChaveRequest.toModel(): NovaChavePixDto {

    return NovaChavePixDto(
        clienteId = this.clienteId,
        chave = this.chave,
        tipoChave = when (this.tipoChave) {
            br.com.zup.ednelson.TipoChave.TIPO_CHAVE_DESCONHECIDO -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        tipoConta = when (this.tipoConta) {
            TipoConta.TIPO_CONTA_DESCONHECIDO -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }

    )

}