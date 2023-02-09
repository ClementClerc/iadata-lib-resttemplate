package fr.toulouse.iadata.resttemplate.handler

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import java.io.IOException

@Component
class RestTemplateResponseErrorHandler : ResponseErrorHandler {
    @Throws(IOException::class)
    override fun hasError(httpResponse: ClientHttpResponse): Boolean {
        return httpResponse.statusCode.isError
    }

    @Throws(IOException::class)
    override fun handleError(httpResponse: ClientHttpResponse)
    {

    }
}
