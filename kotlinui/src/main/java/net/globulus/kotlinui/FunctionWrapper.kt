package net.globulus.kotlinui

import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter

data class FunctionWrapper<R>(
        val function: KFunction<R>,
        val wrapper: (arg: Any?) -> Any
) : KCallable<R> {
    override val annotations = function.annotations
    override val isAbstract = function.isAbstract
    override val isFinal = function.isFinal
    override val isOpen = function.isOpen
    override val isSuspend = function.isSuspend
    override val name = function.name
    override val parameters = function.parameters
    override val returnType = function.returnType
    override val typeParameters = function.typeParameters
    override val visibility = function.visibility

    override fun call(vararg args: Any?): R {
        val receiver= if (function.instanceParameter != null || function.extensionReceiverParameter != null) {
            args[0]
        } else {
            null
        }
        return if (receiver == null) {
            function.call(wrapper(args[0]))
        } else {
            function.call(receiver, wrapper(args[1]))
        }
    }

    override fun callBy(args: Map<KParameter, Any?>): R {
        throw IllegalAccessException("callBy is not currently supported in ${this::class.java.name}")
    }
}

fun <R> wrap(f: KFunction<R>, wrapper: (arg: Any?) -> Any) = FunctionWrapper(f, wrapper)
