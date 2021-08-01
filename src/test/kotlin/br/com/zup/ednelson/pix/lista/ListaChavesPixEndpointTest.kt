package br.com.zup.ednelson.pix.lista

import br.com.zup.ednelson.ConsultaChavePixServiceGrpc
import br.com.zup.ednelson.ListaChavePixServiceGrpc
import br.com.zup.ednelson.ListaChavesPixRequest
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaChavesPixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: ListaChavePixServiceGrpc.ListaChavePixServiceBlockingStub,
) {
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(clienteId = CLIENTE_ID, chave = "02467781054", tipo = TipoChave.CPF))
        repository.save(chave(clienteId = CLIENTE_ID, chave = "rafael@gmail.com", tipo = TipoChave.EMAIL))
        repository.save(chave(clienteId = CLIENTE_ID, chave = "+5513922334455", tipo = TipoChave.CELULAR))
        repository.save(chave(clienteId = CLIENTE_ID, chave = UUID.randomUUID().toString(), tipo = TipoChave.ALEATORIA))
        repository.save(chave(clienteId = UUID.randomUUID(), chave = UUID.randomUUID().toString(), tipo = TipoChave.ALEATORIA))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar chaves pix de um cliente`() {
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClientId(CLIENTE_ID.toString())
            .build())

        with(response) {
            assertTrue(chavesList.size == 4)
        }
    }

    @Test
    fun `deve retornar lista vazia se nao houver chaves cadastradas para o cliente`() {

        val outroClienteId = UUID.randomUUID().toString()

        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClientId(outroClienteId)
            .build())

        with(response) {
            assertTrue(chavesList.isEmpty())
        }
    }

    @Test
    fun `nao deve listar chaves quando clienteId nao for vazio`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setClientId("")
                .build())
        }
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("ClienteId não pode ser vazio ou nulo", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaChavePixServiceGrpc.ListaChavePixServiceBlockingStub? {
            return ListaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            chave = chave,
            tipoChave = tipo,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numeroDaConta = "291900"
            ),
        )
    }
}