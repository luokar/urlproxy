package com.luokar.urlproxy

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebServlet("/http:/*", "/https:/*")
class ProxyServlet(val client: OkHttpClient) : HttpServlet() {
    @Throws(ServletException::class)
    override fun init() {

    }

    @Throws(ServletException::class, IOException::class)
    public override fun service(req: HttpServletRequest, resp: HttpServletResponse) {

        val cachedRequest = CachedRequest(req)
        println("Request:$cachedRequest")

        val okhttpRequest = createOkHttpRequest(cachedRequest)
        val remoteResponse = client.newCall(okhttpRequest).execute()
        val response = CachedResponse(remoteResponse)
        writeToHttpServletResponse(response, resp)
        println("Response:${response}")
    }

    override fun destroy() {
        // do nothing.
    }

    private fun createOkHttpRequest(request: CachedRequest): Request {
        val headerBuilder = Headers.Builder()
        request.modifiedHeaders.forEach { (key, values) ->
            for (value in values) {
                headerBuilder.add(key, value)
            }
        }

        val requestBody = if (request.hasBody) {
            request.contentAsByteArray.toRequestBody(request.contentType?.toMediaType())
        } else {
            null
        }

        val method = request.method
        val queryString = if (request.queryString != null) {
            "?${request.queryString}"
        } else {
            ""
        }
        val remoteRequestUri = request.remotePath
        return Request.Builder()
            .method(method, requestBody)
            .headers(headerBuilder.build())
            .url("$remoteRequestUri$queryString")
            .build()
    }

    private fun writeToHttpServletResponse(cachedResponse: CachedResponse, resp: HttpServletResponse) {
        resp.status = cachedResponse.code
        cachedResponse.modifiedHeaders.forEach { (key, values) ->
            values.forEach { value ->
                resp.setHeader(key, value)
            }
        }
        if (cachedResponse.hasBody) {
            val out = resp.outputStream
            out.write(cachedResponse.contentAsByteArray)
        }
    }
}