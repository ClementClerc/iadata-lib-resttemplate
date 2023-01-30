package fr.toulouse.iadata.resttemplate.service

import fr.toulouse.iadata.resttemplate.RestTemplateProxyCustomizer
import fr.toulouse.iadata.resttemplate.beans.HeaderRequestInterceptor
import fr.toulouse.iadata.resttemplate.config.JsonAuthentCustomizer
import fr.toulouse.iadata.resttemplate.config.RestTemplateConfig
import fr.toulouse.iadata.resttemplate.constants.RestTemplateConstants
import fr.toulouse.iadata.resttemplate.handler.RestTemplateResponseErrorHandler
import mu.KLogger
import mu.KotlinLogging
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
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate

class CustomRestTemplateBuilder(){

    private var restTemplateBuilder: RestTemplateBuilder = RestTemplateBuilder()
    private val log: KLogger = KotlinLogging.logger {  }

    fun proxy(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        restTemplateBuilder = restTemplateBuilder
            .additionalCustomizers(RestTemplateProxyCustomizer(restTemplateConfig.proxyConfig))
        return this
    }



    fun basicRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        val interceptors: MutableList<ClientHttpRequestInterceptor> = ArrayList()


        interceptors.add(
            BasicAuthenticationInterceptor(
                restTemplateConfig.login,
                restTemplateConfig.password
            )
        )
        restTemplateBuilder
            .additionalInterceptors(interceptors)
            .additionalMessageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))
        return this

    }

    fun bearerRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        val interceptors: MutableList<ClientHttpRequestInterceptor> = mutableListOf(HeaderRequestInterceptor(
            HttpHeaders.AUTHORIZATION,
            RestTemplateConstants.SECURITY_BEARER_WITHESPACE + restTemplateConfig.token
        ))
        restTemplateBuilder.additionalInterceptors(interceptors)
        return this

    }

    fun jsonAuthentRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder
    {
        val interceptors: MutableList<ClientHttpRequestInterceptor> = ArrayList()
        restTemplateBuilder.additionalCustomizers(
            JsonAuthentCustomizer(restTemplateConfig)
        ).additionalInterceptors(
            HeaderRequestInterceptor(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
        )

        return this
    }

    fun noAuthRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {


        restTemplateBuilder.additionalMessageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))



        return this
    }

    fun opendataRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        restTemplateBuilder.additionalCustomizers(
            RestTemplateProxyCustomizer(
                restTemplateConfig.proxyConfig
            )
        )
        return this
    }

    fun apiDataRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder
    {
        log.info("[CONFIG-REST] Config used: rest template for data ingest")
        val interceptors: MutableList<ClientHttpRequestInterceptor> = ArrayList()
        if (restTemplateConfig.token != null) {
            log.info("[CONFIG-REST] Token api is not null : use a bearer rest template")
            interceptors.add(
                HeaderRequestInterceptor(
                    HttpHeaders.AUTHORIZATION,
                    restTemplateConfig.authorizationHeaderName + restTemplateConfig.token
                )
            )
            interceptors.add(
                HeaderRequestInterceptor(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            )
            restTemplateBuilder.additionalInterceptors(interceptors)
        } else {
            log.info("[CONFIG-REST] No token found: use a non-authenticated rest template")
        }
        restTemplateBuilder.additionalMessageConverters( StringHttpMessageConverter(StandardCharsets.UTF_8))
        return this
    }

    fun customRestTemplate(
        interceptors: MutableList<ClientHttpRequestInterceptor>,
        restTemplate: RestTemplate,
        restTemplateConfig: RestTemplateConfig
    ): RestTemplate {

        restTemplateConfig.customHeaders?.forEach { key, value ->
            interceptors.add(
                HeaderRequestInterceptor(
                    key,
                    value
                )
            )
        }
        restTemplateConfig.customQueryParam?.forEach { key, value ->
            interceptors.add(
                HeaderRequestInterceptor(
                    key,
                    value
                )
            )
        }

        log.info("[CONFIG-REST] Header with name ${restTemplateConfig.customHeaders.keys}")

        val listInterceptors = restTemplate.interceptors
        listInterceptors.addAll(interceptors)
        restTemplate.interceptors = listInterceptors
        restTemplate.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
        restTemplate.errorHandler = RestTemplateResponseErrorHandler()
        return restTemplate

    }



    fun disableSSL() : CustomRestTemplateBuilder {
        val acceptingTrustStrategy =
            TrustStrategy { chain: Array<X509Certificate?>?, authType: String? -> true }
        val sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val csf = SSLConnectionSocketFactory(sslContext)
        val httpClient = HttpClients.custom().setSSLSocketFactory(csf).build()
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.httpClient = httpClient
        restTemplateBuilder.requestFactory{requestFactory}
        return this
    }

    fun build():RestTemplate{
        return restTemplateBuilder.build()
    }
}