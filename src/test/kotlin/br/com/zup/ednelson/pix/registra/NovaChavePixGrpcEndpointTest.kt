package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.ChavePixServiceGrpc
import br.com.zup.ednelson.ChaveRequest
import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.clients.ItauClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class NovaChavePixGrpcEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: ChavePixServiceGrpc.ChavePixServiceBlockingStub,
) {

    @field:Inject
    lateinit var itauClient: ItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar uma chave pix`() {
        //cenario

        val intituicaoResponse = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190")
        val titularResponse = TitularResponse(nome = "Rafael M C Ponte", cpf = "02467781054")
        val dadosDaContaResponse = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = intituicaoResponse,
            agencia = "1234",
            numero = "222222",
            titular = titularResponse,
        )

        `when`(itauClient.buscaContaPorIdClienteETipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

        //ação
        val response = grpcClient.cadastrar(ChaveRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setChave("02467781054")
            .setTipoChave(TipoChave.CPF)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build())

        //corretude

        with(response) {
            assertNotNull(chavePixId)
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertTrue(repository.existsByChavePixId(chavePixId)) // efeito colateral
        }

    }

    @Test
    fun `nao deve cadastrar uma chave pix existente`() {

        val existente = repository.save(ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            chave = "02467781054",
            tipoChave = br.com.zup.ednelson.pix.registra.TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada("", "", "", "", "")
        ))

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(ChaveRequest.newBuilder()
                .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setChave(existente.chave)
                .setTipoChave(TipoChave.CPF)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }

        assertTrue(repository.count() < 2) // efeito colareral

    }

    @Test
    fun `nao deve cadastrar uma chave pix quando nao encontrar dados da conta do cliente`() {
        //cenario

        `when`(itauClient.buscaContaPorIdClienteETipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(ChaveRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setChave("02467781054")
                .setTipoChave(TipoChave.CPF)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `nao deve cadastrar uma chave pix quando os parametros forem invalidos`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(ChaveRequest.newBuilder().build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ChavePixServiceGrpc.ChavePixServiceBlockingStub? {
            return ChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

}