package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.ChavePixServiceGrpc
import br.com.zup.ednelson.ChaveRequest
import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.clients.*
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
import org.mockito.Mockito.*
import java.time.LocalDateTime
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

    @field:Inject
    lateinit var bcbClient: BcbClient

    lateinit var responseItau: DadosDaContaResponse
    lateinit var requestBcb: CreatePixKeyRequest
    lateinit var responseBcb: CreatePixKeyResponse

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        responseItau = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
            agencia = "1234",
            numero = "222222",
            titular = TitularResponse(nome = "Rafael M C Ponte", cpf = "02467781054"),
        )
        requestBcb = CreatePixKeyRequest(
            keyType = "CPF",
            key = "02467781054",
            bankAccount = BankAccount(
                participant = responseItau.instituicao.ispb,
                branch = responseItau.agencia,
                accountNumber = responseItau.numero,
                accountType = "CACC"),
            owner = Owner(
                type = "NATURAL_PERSON",
                name = responseItau.titular.nome,
                taxIdNumber = responseItau.titular.cpf
            )
        )
        responseBcb = CreatePixKeyResponse(
            keyType = requestBcb.keyType,
            key = requestBcb.key,
            bankAccount = requestBcb.bankAccount,
            owner = requestBcb.owner,
            createdAt = LocalDateTime.now().toString()
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar uma chave pix`() {
        //cenário
        `when`(itauClient.buscaContaPorIdClienteETipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(responseItau))

        `when`(bcbClient.cadastra(requestBcb))
            .thenReturn(HttpResponse.created(responseBcb))

        //ação
        val response = grpcClient.cadastrar(ChaveRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setChave("02467781054")
            .setTipoChave(TipoChave.CPF)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build())

        //validação
        with(response) {
            assertNotNull(chavePixId)
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertTrue(repository.existsByChavePixId(UUID.fromString(chavePixId))) // efeito colateral
        }

    }

    @Test
    fun `nao deve cadastrar uma chave pix quando houver erro no registro do bcb`() {
        //cenário
        `when`(itauClient.buscaContaPorIdClienteETipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(responseItau))

        `when`(bcbClient.cadastra(requestBcb))
            .thenReturn(HttpResponse.badRequest(responseBcb))

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(ChaveRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setChave("02467781054")
                .setTipoChave(TipoChave.CPF)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        //validação

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave pix no Banco Central do Brasil (BCB)", status.description)
        }

        assertTrue(repository.count() < 1)
    }

    @Test
    fun `nao deve cadastrar uma chave pix existente`() {

        val existente = repository.save(ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            chave = "02467781054",
            tipoChave = br.com.zup.ednelson.pix.registra.TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada("", "", "", "", "","")
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

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}