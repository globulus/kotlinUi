package net.globulus.kotlinui

import net.globulus.kotlinui.annotation.State
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class StateInvocationHandler<T: KView>(private val instance: T) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        val clazz = instance::class.java
        val clazzMethod = clazz.getMethod(method.name, *method.parameterTypes)
        val annotation = clazzMethod.getAnnotation(State::class.java)
        val result = clazzMethod.invoke(instance, args)
        annotation?.let {
            instance.triggerObserver(method.name)
        }
        return if (result == instance)
            proxy
        else
            result
    }
}