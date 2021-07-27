package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.RemoveChavePixRequest
import br.com.zup.ednelson.RemoveChavePixServiceGrpc
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.ChavePix
import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.registra.ContaAssociada
import br.com.zup.ednelson.pix.registra.TipoChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChavePixGrpcEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub,
) {
    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {
        //cenário
        val existente = repository.save(ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            chave = "02467781054",
            tipoChave = TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada("", "", "", "", "")
        ))

        //ação
        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setChavePixId(existente.chavePixId.toString())
            .setClienteId(existente.clienteId.toString())
            .build()
        )

        //validação
        assertEquals(existente.chavePixId.toString(), response.chavePixId.toString())
    }

    @Test
    fun `deve retornar not found quando nao encontrar uma chave`(){

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId("2229c646-d794-456c-8dfa-38e0e72a0b09")
                .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
        assertFalse(repository.count() > 0) // efeito colareral
    }

    @Test
    fun `nao deve remover uma chave pix quando os parametros forem invalidos`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub? {
            return RemoveChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}