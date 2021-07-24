package br.com.zup.ednelson.pix.registra


import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(valor: String?): Boolean {
            if (valor.isNullOrBlank()) {
                return false
            }

            if (!valor.matches("^[0-9]{11}\$".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(valor, null)
            }
        }
    },

    CELULAR {
        override fun valida(valor: String?): Boolean {
            if (valor.isNullOrBlank()) {
                return false
            }
            return valor.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },

    EMAIL {
        override fun valida(valor: String?): Boolean {
            if (valor.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(valor, null)
            }
        }
    },

    ALEATORIA {
        override fun valida(valor: String?) = valor.isNullOrBlank()
    };

    abstract fun valida(valor: String?): Boolean

}
