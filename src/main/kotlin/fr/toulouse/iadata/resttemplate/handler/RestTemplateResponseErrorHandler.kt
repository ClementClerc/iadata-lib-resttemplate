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
        return (httpResponse.statusCode.series() == HttpStatus.Series.CLIENT_ERROR
                || httpResponse.statusCode.series() == HttpStatus.Series.SERVER_ERROR)
    }

    @Throws(IOException::class)
    override fun handleError(httpResponse: ClientHttpResponse) {
        if (httpResponse.statusCode.series() == HttpStatus.Series.SERVER_ERROR) {
            // handle SERVER_ERROR
        } else if (httpResponse.statusCode.series() == HttpStatus.Series.CLIENT_ERROR) {
            // handle CLIENT_ERROR
            if (httpResponse.statusCode == HttpStatus.NOT_FOUND) {
            }
        }
    }
}
