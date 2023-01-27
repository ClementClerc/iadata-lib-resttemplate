package fr.toulouse.iadata.resttemplate.beans

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException


class HeaderRequestInterceptor(private val headerName: String, private val headerValue: String) :
    ClientHttpRequestInterceptor {
    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers[headerName] = headerValue
        return execution.execute(request, body)
    }
}