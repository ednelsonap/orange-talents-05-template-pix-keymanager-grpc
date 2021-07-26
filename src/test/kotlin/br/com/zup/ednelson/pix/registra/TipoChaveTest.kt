package br.com.zup.ednelson.pix.registra

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MicronautTest
internal class TipoChaveTest {

    @Nested
    inner class CPF {
        @Test
        fun `deve ser valido quando cpf for um numero valido`() {
            with(TipoChave.CPF) {
                assertTrue(valida(valor = "62578364842"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for um numero invalido`() {
            with(TipoChave.CPF) {
                assertFalse(valida(valor = "62578364841"))
            }
        }

        @Test
        fun `nao deve ser valido quando o cpf nao for informado`() {
            with(TipoChave.CPF) {
                assertFalse(valida(valor = null))
                assertFalse(valida(valor = ""))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `deve ser valido quando o email for valido`() {
            with(TipoChave.EMAIL) {
                assertTrue(valida(valor = "teste@teste.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando email for invalido`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(valor = "teste.teste.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando o email nao for informado`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(valor = null))
                assertFalse(valida(valor = ""))
            }
        }
    }

    @Nested
    inner class CELULAR {
        @Test
        fun `deve ser valido quando o numero do celular for valido`() {
            with(TipoChave.CELULAR) {
                assertTrue(valida(valor = "+5585988714077"))
            }
        }

        @Test
        fun `nao deve ser valido quando o numero do celular for invalido`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(valor = "5585988714077"))
            }
        }

        @Test
        fun `nao deve ser valido quando o numero do celular nao for informado`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(valor = null))
                assertFalse(valida(valor = ""))
            }
        }
    }

    @Nested
    inner class ALEATORIA {
        @Test
        fun `nao deve ser valido quando chave aleatoria for nula ou vazia`() {
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(valor = null))
                assertTrue(valida(valor = ""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave aleatoria possuir um valor`() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida(valor = "qualquer coisa"))
            }
        }
    }

}