package it.polito.loginservice.utils

import it.polito.loginservice.interceptors.RateLimiterInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AppConfig : WebMvcConfigurer {
    private val interceptor: RateLimiterInterceptor = RateLimiterInterceptor()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/user/**")
    }
}