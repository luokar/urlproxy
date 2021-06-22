package com.luokar.urlproxy

import org.apache.commons.io.IOUtils
import org.springframework.http.HttpHeaders
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest

data class CachedRequest(private val request: HttpServletRequest) :
    HttpServletRequest by request {

    val hasBody = request.contentLengthLong > -1
    val contentAsByteArray = IOUtils.toByteArray(request.inputStream)

    val remotePath: String
        get() {
            return request.requestURI.substring(1)
        }

    val headers: HttpHeaders
        get() {
            val httpHeaders = HttpHeaders()
            request.headerNames.toList().forEach { header ->
                request.getHeaders(header).toList().forEach { value ->
                    httpHeaders.add(header, value)
                }
            }
            return httpHeaders
        }

    val modifiedHeaders: HttpHeaders
        get() {
            val httpHeaders = headers
            httpHeaders.remove(HttpHeaders.HOST)
            httpHeaders.remove(HttpHeaders.EXPECT)
            return httpHeaders
        }

    val contentAsString: String
        get() {
            return ResponseUtils.decode(
                inputStream = contentAsByteArray.inputStream(),
                encoding = request.getHeader(HttpHeaders.CONTENT_ENCODING),
                charset = request.characterEncoding?.let { Charset.forName(it) }
            )
        }

    override fun toString(): String {
        return "CachedRequest(method=${request.method},path='${remotePath}',query=${request.queryString},headers=${headers},body=${contentAsString})"
    }
}