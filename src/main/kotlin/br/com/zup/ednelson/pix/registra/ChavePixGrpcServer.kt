package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.*
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChavePixGrpcServer(@Inject val service: NovaChavePixService, val chaveRepository: ChaveRepository) : ChavePixServiceGrpc.ChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(ChavePixGrpcServer::class.java)

    override fun cadastrar(request: ChaveRequest?, responseObserver: StreamObserver<ChaveResponse>?) {

        logger.info("Cadastrando a chave: $request")

        //val clienteId = request?.clienteId
        //val valor = request?.valor
        //val tipoChave = request?.tipoChave
        //val tipoConta = request?.tipoConta

        //val novaChave = NovaChavePixDto(clienteId, valor, tipoChave.toString(), tipoConta.toString())
        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)

       /* val clienteId = request?.clienteId
        if (clienteId == null || clienteId.isBlank() ) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("clienteId deve ser informado")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        val tipoChave = request?.tipoChave
        if (tipoChave == TipoChave.TIPO_CHAVE_DESCONHECIDO) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("tipoChave deve ser informado")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        val tipoConta = request?.tipoConta
        if (tipoConta == TipoConta.TIPO_CONTA_DESCONHECIDO) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("tipoConta deve ser informado")
                .asRuntimeException()
            responseObserver?.onError(e)
        }*/

        val response = ChaveResponse.newBuilder()
            .setChavePixId(chaveCriada.pixId)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()

        logger.info("Chave cadastrada")
    }
}

