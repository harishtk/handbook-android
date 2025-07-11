package com.handbook.app.core.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val aiaDispatcher: AiaDispatchers)

enum class AiaDispatchers {
    Default, Io, Main
}