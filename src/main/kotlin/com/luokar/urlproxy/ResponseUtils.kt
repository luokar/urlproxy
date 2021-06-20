package com.luokar.urlproxy

import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.brotli.dec.BrotliInputStream
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

object ResponseUtils {

    fun decode(inputStream: InputStream, encoding: String? = null, charset: Charset? = null): String {
        val decompressedInputStream = uncompress(inputStream, encoding)
        val bomInputStream = BOMInputStream(BufferedInputStream(decompressedInputStream))
        return IOUtils.toString(bomInputStream, charset ?: Charsets.UTF_8)
    }

    fun getAllResponses(response: CachedResponse): List<CachedResponse> {
        val responses = ArrayList<CachedResponse>()
        responses.add(response)
        var ptr = response
        while (ptr.priorResponse != null) {
            responses.add(ptr.priorResponse!!)
            ptr = ptr.priorResponse!!
        }
        return responses
    }

    private fun uncompress(inputStream: InputStream, encoding: String?): InputStream {
        return when {
            encoding.equals("br", ignoreCase = true) ->
                BrotliInputStream(inputStream)
            encoding.equals("gzip", ignoreCase = true) ->
                GZIPInputStream(inputStream)
            encoding.equals("deflate", ignoreCase = true) ->
                InflaterInputStream(inputStream)
            else -> return inputStream
        }
    }
}