package com.luokar.urlproxy

import okhttp3.Response
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_ENCODING

class CachedResponse(private val response: Response) {

    val headers: HttpHeaders
        get() {
            val httpHeaders = HttpHeaders()
            response.headers.forEach { (k, v) ->
                httpHeaders.add(k, v)
            }
            return httpHeaders
        }

    val modifiedHeaders: HttpHeaders
        get() {
            val httpHeaders = headers
            httpHeaders.remove(HttpHeaders.TRANSFER_ENCODING)
            return httpHeaders
        }

    val hasBody = response.body?.source()?.exhausted() == false
    val contentAsByteArray = response.body?.source()?.readByteArray() ?: ByteArray(0)
    val contentAsString: String
        get() {
            val charset = response.body?.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            return ResponseUtils.decode(
                inputStream = contentAsByteArray.inputStream(),
                encoding = headers.get(CONTENT_ENCODING)?.firstOrNull(),
                charset = charset
            )
        }

    val code: Int get() = response.code

    val priorResponse get() = if (response.priorResponse != null) CachedResponse(response.priorResponse!!) else null

    override fun toString(): String {
        return if (this.hasBody) {
            "Response(status=${code},headers=${headers},body='${contentAsString}')"
        } else {
            "Response(status=${code},headers=${headers})"
        }
    }
}