package com.luokar.urlproxy

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.headersContentLength
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpHeaders.ACCEPT_ENCODING
import org.springframework.http.HttpHeaders.HOST
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebServlet("/http:/*", "/https:/*")
class ProxyServlet() : HttpServlet() {

    companion object {
        val M = 1024L * 1024 * 1024
    }


    @Throws(ServletException::class)
    override fun init() {

    }

    @Throws(ServletException::class, IOException::class)
    public override fun service(req: HttpServletRequest, resp: HttpServletResponse) {

        val remoteRequest = createRequest(req)
        val client = OkHttpClient
            .Builder()
            .hostnameVerifier { _, _ -> true }
            .build()
        println("Request Header:$remoteRequest")

        val remoteResponse = client.newCall(remoteRequest).execute()
        println("Response Header:${remoteResponse} ${remoteResponse.headers}")
        resp.status = remoteResponse.code
        remoteResponse.headers.forEach { (k, v) ->
            resp.setHeader(k, v)
        }

        if (remoteResponse.body != null) {
            val bs = remoteResponse.body!!.byteStream()
            val out = resp.outputStream
            if (remoteResponse.headersContentLength() < 1 * M) {
                val body = IOUtils.toByteArray(bs)
                val charset = remoteResponse.body!!.contentType()?.charset()
                remoteResponse.body
                val bodyAsString = if (charset != null) {
                    String(body, charset)
                } else {
                    String(body)
                }
                println("Response body:${bodyAsString}")
                IOUtils.write(body, out)
            } else {
                println("Response body is too big")
                bs.copyTo(out)
            }
        }
    }

    override fun destroy() {
        // do nothing.
    }

    fun createRequest(request: HttpServletRequest): Request {
        val headerBuilder = Headers.Builder()
        request.headerNames.toList().forEach { header ->
            if (header.equals(HOST, ignoreCase = true) || header.equals(ACCEPT_ENCODING, ignoreCase = true)) {
                return@forEach
            }
            request.getHeaders(header).toList().forEach { value ->
                headerBuilder.add(header, value)
            }
        }
        headerBuilder.add(ACCEPT_ENCODING, "identity")

        val requestBody = if (request.contentLength != -1) {
            val originalBody = ByteArray(request.contentLength)
            IOUtils.read(request.inputStream, originalBody)
            println("Request Body: ${String(originalBody)}")
            originalBody.toRequestBody(request.contentType?.toMediaType())
        } else {
            null
        }

        val method = request.method
        val queryString = if (request.queryString != null) {
            "?${request.queryString}"
        } else {
            ""
        }
        val remoteRequestUri = request.requestURI.substring(1)
        return Request.Builder()
            .method(method, requestBody)
            .headers(headerBuilder.build())
            .url("$remoteRequestUri$queryString")
            .build()
    }
}