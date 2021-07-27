package br.com.zup.ednelson.pix.compartilhado

import br.com.zup.ednelson.pix.registra.exception.ChavePixExistenteException
import br.com.zup.ednelson.pix.registra.exception.ClienteNaoEncontradoNoItauException
import br.com.zup.ednelson.pix.registra.exception.TipoDeChaveDesconhecidoException
import br.com.zup.ednelson.pix.registra.exception.TipoDeContaDesconhecidoException
import br.com.zup.ednelson.pix.remove.exception.ChaveNaoEncontradaException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        return try {
            return context.proceed()
        } catch (e: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (e) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(e)
                    .withDescription(e.message)

                is ChavePixExistenteException -> Status.ALREADY_EXISTS
                    .withCause(e)
                    .withDescription("Chave existente")

                is TipoDeChaveDesconhecidoException -> Status.INVALID_ARGUMENT
                    .withCause(e)
                    .withDescription("Tipo de chave desconhecido")

                is TipoDeContaDesconhecidoException -> Status.INVALID_ARGUMENT
                    .withCause(e)
                    .withDescription("Tipo de conta desconhecido")

                is ClienteNaoEncontradoNoItauException -> Status.NOT_FOUND
                    .withCause(e)
                    .withDescription(e.message)

                is ChaveNaoEncontradaException -> Status.NOT_FOUND
                    .withCause(e)
                    .withDescription(e.message)

                else -> Status.UNKNOWN
                    .withCause(e)
                    .withDescription("Erro inesperado")
            }

            responseObserver.onError(status.asRuntimeException())
        }
    }

}