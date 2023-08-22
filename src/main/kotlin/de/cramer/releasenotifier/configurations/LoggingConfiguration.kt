package de.cramer.releasenotifier.configurations

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class LoggingConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun logger(injectionPoint: InjectionPoint): Logger = LoggerFactory.getLogger(injectionPoint.containingClass)

    private val InjectionPoint.containingClass: Class<*>
        get() = methodParameter?.containingClass ?: field?.declaringClass ?: error("no containing class found")
}
