package com.luokar.urlproxy

import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

@SpringBootApplication
@ServletComponentScan
class UrlProxyApplication {
    @Bean
    fun client(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .writeTimeout(1800, TimeUnit.SECONDS)
            .readTimeout(1800, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<UrlProxyApplication>(*args)
}
