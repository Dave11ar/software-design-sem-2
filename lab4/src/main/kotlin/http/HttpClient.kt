package http

import java.io.Closeable

interface HttpClient : Closeable {
    fun getResponse(url: String): String
}
