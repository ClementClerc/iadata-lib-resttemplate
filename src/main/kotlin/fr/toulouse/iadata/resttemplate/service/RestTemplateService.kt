package fr.toulouse.iadata.resttemplate.service

import fr.toulouse.iadata.resttemplate.RestTemplateProxyCustomizer
import fr.toulouse.iadata.resttemplate.beans.HeaderRequestInterceptor
import fr.toulouse.iadata.resttemplate.config.JsonAuthentCustomizer
import fr.toulouse.iadata.resttemplate.config.RestTemplateConfig
import fr.toulouse.iadata.resttemplate.constants.RestTemplateConstants
import fr.toulouse.iadata.resttemplate.exception.NoRestTemplateFoundException
import fr.toulouse.iadata.resttemplate.handler.RestTemplateResponseErrorHandler
import mu.KLogger
import mu.KotlinLogging
import org.apache.catalina.core.ApplicationContext
import org.apache.http.HttpHeaders
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.apache.http.ssl.TrustStrategy
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.cglib.core.Customizer
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate

@Service
class RestTemplateService(
    private val context : GenericWebApplicationContext,
)
{
    private val log: KLogger = KotlinLogging.logger {  }
    private val defaultRestTemplate = RestTemplate()

    fun getRestTemplate( beanName : String ) : RestTemplate
    {
        return try {
            context.getBean( beanName ) as RestTemplate
        }
        catch ( e : Exception )
        {
            log.error ("[REST TEMPLATE SERVICE] No rest template found for bean name $beanName", e )
            defaultRestTemplate
        }
    }

    fun getRestTemplateDefault( ) : RestTemplate
    {
        return defaultRestTemplate
    }
}