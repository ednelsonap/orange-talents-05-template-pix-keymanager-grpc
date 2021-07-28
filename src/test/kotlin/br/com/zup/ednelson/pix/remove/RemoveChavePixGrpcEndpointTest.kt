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
import org.junit.jupiter.api.AfterEach
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

    lateinit var chaveExistente: ChavePix

    @BeforeEach
    fun setup() {
        chaveExistente = repository.save(ChavePix(
            clienteId = UUID.randomUUID(),
            chave = "02467781054",
            tipoChave = TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada("", "", "", "", "")
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {

        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setChavePixId(chaveExistente.chavePixId.toString())
            .setClienteId(chaveExistente.clienteId.toString())
            .build()
        )

        assertEquals(chaveExistente.chavePixId.toString(), response.chavePixId.toString())
    }

    @Test
    fun `nao deve remover chave pix quando ela for inexistente`(){

        val chavePixIdInexistente = UUID.randomUUID().toString()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId(chavePixIdInexistente)
                .setClienteId(chaveExistente.clienteId.toString())
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
        assertFalse(repository.count() > 1)
    }

    @Test
    fun `nao deve remover chave pix quando ela existe mas pertence a outro cliente`(){

        val outroClienteId = UUID.randomUUID().toString()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId(chaveExistente.chavePixId.toString())
                .setClienteId(outroClienteId)
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
        assertFalse(repository.count() > 1)
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