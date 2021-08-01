package br.com.zup.ednelson.pix.lista

import br.com.zup.ednelson.*
import br.com.zup.ednelson.pix.compartilhado.ErrorHandler
import br.com.zup.ednelson.pix.registra.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesPixEndpoint(@Inject private val repository: ChavePixRepository)
    : ListaChavePixServiceGrpc.ListaChavePixServiceImplBase() {

    override fun lista(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>,
    ) {
        if (request.clientId.isNullOrBlank())
            throw IllegalArgumentException("ClienteId não pode ser vazio ou nulo")

        val clienteId = UUID.fromString(request.clientId)
        val chaves = repository.findAllByClienteId(clienteId).map {
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setChavePixId(it.chavePixId.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }
}