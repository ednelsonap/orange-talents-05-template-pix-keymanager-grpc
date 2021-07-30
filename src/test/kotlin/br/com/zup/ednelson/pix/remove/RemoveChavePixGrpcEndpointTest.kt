package br.com.zup.ednelson.pix.remove

import br.com.zup.ednelson.RemoveChavePixRequest
import br.com.zup.ednelson.RemoveChavePixServiceGrpc
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.ChavePix
import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.registra.ContaAssociada
import br.com.zup.ednelson.pix.registra.TipoChave
import br.com.zup.ednelson.pix.registra.clients.BcbClient
import br.com.zup.ednelson.pix.registra.clients.DeletePixKeyRequest
import br.com.zup.ednelson.pix.registra.clients.DeletePixKeyResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse

import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChavePixGrpcEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub,
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    lateinit var chaveExistente: ChavePix

    @BeforeEach
    fun setup() {
        chaveExistente = repository.save(ChavePix(
            clienteId = UUID.randomUUID(),
            chave = "02467781054",
            tipoChave = TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada("", "", "", "", "", "")
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {
        // cenário
        `when`(bcbClient.deleta(key = "02467781054", DeletePixKeyRequest(
            key = "02467781054",
            participant = ""
        )))
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(
                key = "02467781054",
                participant = "",
                deletedAt = LocalDateTime.now().toString()
            )))

        // ação
        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setChavePixId(chaveExistente.chavePixId.toString())
            .setClienteId(chaveExistente.clienteId.toString())
            .build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.chavePixId.toString(), chavePixId)
        }

        assertTrue(repository.count() < 1)
    }

    @Test
    fun `nao deve remover chave pix existente quando ocorrer erro no bcb`() {
        // cenário
        `when`(bcbClient.deleta(key = "02467781054", DeletePixKeyRequest(
            key = "02467781054",
            participant = ""
        )))
            .thenReturn(HttpResponse.badRequest(DeletePixKeyResponse(
                key = "02467781054",
                participant = "",
                deletedAt = LocalDateTime.now().toString()
            )))

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId(chaveExistente.chavePixId.toString())
                .setClienteId(chaveExistente.clienteId.toString())
                .build()
            )
        }

        // validação
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave pix no Banco Central do Brasil (BCB)", status.description)
        }

        assertFalse(repository.count() < 1)
    }

    @Test
    fun `nao deve remover chave pix quando ela for inexistente`() {

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
    fun `nao deve remover chave pix quando ela existe mas pertence a outro cliente`() {

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

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }
}