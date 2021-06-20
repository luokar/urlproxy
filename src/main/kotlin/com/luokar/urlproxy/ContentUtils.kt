package com.luokar.urlproxy

import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.brotli.dec.BrotliInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

object ContentUtils {

    fun decode(inputStream: InputStream, encoding: String? = null, charset: Charset? = null): String {
        val decompressedInputStream = uncompress(inputStream, encoding)
        val bomInputStream = BOMInputStream(decompressedInputStream)
        return IOUtils.toString(bomInputStream, charset ?: Charsets.UTF_8)
    }

    private fun uncompress(inputStream: InputStream, encoding: String?): InputStream {
        return when {
            encoding.equals("br", ignoreCase = true) ->
                BrotliInputStream(inputStream)
            encoding.equals("gzip", ignoreCase = true) ->
                GZIPInputStream(inputStream)
            else -> return inputStream
        }
    }
}