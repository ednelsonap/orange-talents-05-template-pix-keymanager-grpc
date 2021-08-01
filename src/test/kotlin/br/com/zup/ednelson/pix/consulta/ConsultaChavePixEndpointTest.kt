package br.com.zup.ednelson.pix.consulta

import br.com.zup.ednelson.ConsultaChavePixRequest
import br.com.zup.ednelson.ConsultaChavePixServiceGrpc
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.ChavePix
import br.com.zup.ednelson.pix.registra.ChavePixRepository
import br.com.zup.ednelson.pix.registra.ContaAssociada
import br.com.zup.ednelson.pix.registra.TipoChave
import br.com.zup.ednelson.pix.registra.clients.BankAccount
import br.com.zup.ednelson.pix.registra.clients.BcbClient
import br.com.zup.ednelson.pix.registra.clients.Owner
import br.com.zup.ednelson.pix.registra.clients.PixKeyDetailsResponse
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
import org.junit.jupiter.api.Assertions.assertEquals
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
internal class ConsultaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub,
) {
    @field:Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(clienteId = CLIENTE_ID, chave = "02467781054", tipo = TipoChave.CPF))
        repository.save(chave(clienteId = CLIENTE_ID, chave = "rafael@gmail.com", tipo = TipoChave.EMAIL))
        repository.save(chave(clienteId = CLIENTE_ID, chave = "+5513922334455", tipo = TipoChave.CELULAR))
        repository.save(chave(clienteId = CLIENTE_ID, chave = UUID.randomUUID().toString(), tipo = TipoChave.ALEATORIA))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve consultar chave por pixId e clienteId`() {
        // cenário
        val chaveExistente = repository.findByChave("+5513922334455").get()

        // ação
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setChavePixId(chaveExistente.chavePixId.toString())
                .setClienteId(chaveExistente.clienteId.toString())
                .build()
            ).build())

        // validação
        with(response) {
            assertEquals(chaveExistente.chavePixId.toString(), this.chavePixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipoChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve consultar chave por pixId e clienteId quando filtro invalido`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    @Test
    fun `nao deve consultar chave por pixId e clienteId quando registro nao existir`() {
        // cenário
        val chavePixInexistente = UUID.randomUUID().toString()
        val clienteIdInexistente = UUID.randomUUID().toString()

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setChavePixId(chavePixInexistente)
                    .setClienteId(clienteIdInexistente)
                    .build()
                ).build())
        }

        // validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não enconstrada", status.description)
        }
    }

    @Test
    fun `deve consultar chave por valor da chave quando o registro existir localmente`() {
        // cenário
        val chaveExistente = repository.findByChave("rafael@gmail.com").get()

        // ação
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave("rafael@gmail.com")
            .build())

        // validação
        with(response) {
            assertEquals(chaveExistente.chavePixId.toString(), this.chavePixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipoChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve consultar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {
        // cenário
        val bcbResponse = pixKeyDetailsResponse()
        `when`(bcbClient.findByKey(key = "cliente.outro.banco@santander.com.br"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // ação
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave("cliente.outro.banco@santander.com.br")
            .build())

        // validação
        with(response) {
            assertEquals(bcbResponse.keyType, this.chave.tipoChave.name)
            assertEquals(bcbResponse.key, this.chave.chave)
        }
    }

    @Test
    fun `nao deve consultar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        // cenário
        `when`(bcbClient.findByKey(key = "clientenaoexistente@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave("clientenaoexistente@santander.com.br")
                .build())
        }

       // validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub? {
            return ConsultaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = "EMAIL",
            key = "cliente.outro.banco@santander.com.br",
            bankAccount = BankAccount("60701190","","", accountType = "CACC"),
            owner = Owner("","",""),
            createAt = LocalDateTime.now()
        )
    }
}