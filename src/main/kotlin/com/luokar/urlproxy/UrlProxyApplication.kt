package com.luokar.urlproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@SpringBootApplication
@ServletComponentScan
class UrlProxyApplication

fun main(args: Array<String>) {
    runApplication<UrlProxyApplication>(*args)
}
