package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoConta
import br.com.zup.ednelson.pix.registra.clients.*
import br.com.zup.ednelson.pix.registra.exception.ChavePixExistenteException
import br.com.zup.ednelson.pix.registra.exception.ClienteNaoEncontradoNoItauException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BcbClient,
) {

    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePixDto): ChavePix {

        // Verifica de a chave já existe no sistema
        if (chavePixRepository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException()
        }

        // 1 - Busca dados da conta no ERP do Itaú
        val response = itauClient.buscaContaPorIdClienteETipo(novaChave.clienteId!!, novaChave.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw ClienteNaoEncontradoNoItauException()

        // 2 - Grava no banco
        val chave = novaChave.toModel(conta)
        chavePixRepository.save(chave).also {
            logger.info("Chave gravada no banco de dados local")
        }

        if(chave.tipoChave == TipoChave.ALEATORIA) {
            logger.info("Chave gerada local: ${chave.chave}")
        }

        // 3 - Grava no bcb
        val bankAccount = BankAccount(
            participant = chave.conta.ispb,
            branch = chave.conta.agencia,
            accountNumber = chave.conta.numeroDaConta,
            accountType = if(chave.tipoConta == TipoConta.CONTA_CORRENTE) "CACC" else "SVGS"
        )

        val owner = Owner(
            type = chave.tipoPessoa.name,
            name = chave.conta.nomeDoTitular,
            taxIdNumber = chave.conta.cpfDoTitular
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = when (chave.tipoChave) {
                TipoChave.ALEATORIA -> {
                    "RANDOM"
                }
                TipoChave.CELULAR -> {
                    "PHONE"
                }
                else -> {
                    chave.tipoChave.name
                }
            },
            key = chave.chave,
            bankAccount = bankAccount,
            owner = owner
        )

        val bcbResponse = bcbClient.cadastra(bcbRequest).also {
            logger.info("Chave cadastrada no Banco Central do Brasil")
        }

        // 4 - Atualiza a chave, quando o tipo for aleatória, com a chave gerada pelo BCB
        chave.chave = bcbResponse.body().key
        chavePixRepository.update(chave)

        if(chave.tipoChave == TipoChave.ALEATORIA) {
            logger.info("Chave gerada pelo BCB: ${chave.chave}")
        }

        if (bcbResponse.status != HttpStatus.CREATED)
            throw IllegalStateException("Erro ao registrar chave pix no Banco Central do Brasil (BCB)")

        return chave
    }

}

