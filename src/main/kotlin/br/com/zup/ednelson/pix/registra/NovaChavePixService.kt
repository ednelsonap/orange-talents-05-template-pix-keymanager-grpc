package br.com.zup.ednelson.pix.registra

import br.com.zup.ednelson.TipoChave
import br.com.zup.ednelson.pix.registra.clients.ItauClient
import br.com.zup.ednelson.pix.registra.exception.ChavePixExistenteException
import br.com.zup.ednelson.pix.registra.exception.ClienteNaoEncontradoNoItauException
import br.com.zup.ednelson.pix.registra.exception.TipoDeChaveDesconhecidoException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ItauClient
) {

    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    fun registra(@Valid novaChave: NovaChavePixDto): ChavePix {

        // Verifica de a chave já existe no sistema
        if (chavePixRepository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException()
        }

        // Busca dados da conta no ERP do Itaú
        val response = itauClient.buscaContaPorIdClienteETipo(novaChave.clienteId!!, novaChave.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw ClienteNaoEncontradoNoItauException()

        // Grava no banco
        val chave = novaChave.toModel(conta)
        chavePixRepository.save(chave)

        logger.info("Chave cadastrada")

        return chave
    }

}
