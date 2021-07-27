package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.RemoveChavePixRequest
import br.com.zup.ednelson.RemoveChavePixResponse
import br.com.zup.ednelson.RemoveChavePixServiceGrpc
import br.com.zup.ednelson.pix.compartilhado.ErrorHandler
import br.com.zup.ednelson.pix.registra.NovaChavePixGrpcEndpoint
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixGrpcEndpoint(@Inject val service: RemoveChaveService)
    : RemoveChavePixServiceGrpc.RemoveChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(NovaChavePixGrpcEndpoint::class.java)

    override fun remove(
        request: RemoveChavePixRequest?,
        responseObserver: StreamObserver<RemoveChavePixResponse>?,
    ) {

        service.remove(request!!.chavePixId, request.clienteId)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .setChavePixId(request.chavePixId)
            .build())

        responseObserver?.onCompleted()
    }
}
