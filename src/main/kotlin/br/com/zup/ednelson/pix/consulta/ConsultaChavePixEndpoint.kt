package br.com.zup.ednelson.pix.consulta

import br.com.zup.ednelson.ConsultaChavePixRequest
import br.com.zup.ednelson.ConsultaChavePixRequest.FiltroCase.*
import br.com.zup.ednelson.ConsultaChavePixResponse
import br.com.zup.ednelson.ConsultaChavePixServiceGrpc
import br.com.zup.ednelson.pix.compartilhado.ErrorHandler
import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.registra.clients.BcbClient
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorHandler
@Singleton
class ConsultaChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator
) : ConsultaChavePixServiceGrpc.ConsultaChavePixServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(ConsultaChavePixResponseConversor().converte(chaveInfo))
        responseObserver.onCompleted()
    }
}

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when (filtroCase) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, chavePixId = it.chavePixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}

