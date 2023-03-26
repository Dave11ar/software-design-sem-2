package http

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class ApacheHttpClient(private val client: CloseableHttpClient) : HttpClient {
    override fun getResponse(url: String): String =
        client.execute(HttpGet(url)).use { EntityUtils.toString(it.entity) }

    override fun close() {
        client.close()
    }

    companion object {
        fun getInstance() = ApacheHttpClient(HttpClients.createDefault())
    }
}