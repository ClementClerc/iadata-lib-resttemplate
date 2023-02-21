package fr.toulouse.iadata.resttemplate.service

import fr.toulouse.iadata.resttemplate.RestTemplateProxyCustomizer
import fr.toulouse.iadata.resttemplate.beans.HeaderRequestInterceptor
import fr.toulouse.iadata.resttemplate.config.JsonAuthentCustomizer
import fr.toulouse.iadata.resttemplate.config.RestTemplateConfig
import fr.toulouse.iadata.resttemplate.constants.RestTemplateConstants
import fr.toulouse.iadata.resttemplate.handler.RestTemplateResponseErrorHandler
import mu.KLogger
import mu.KotlinLogging
import org.apache.hc.core5.http.HttpHeaders
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.ssl.TrustStrategy
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.springframework.boot.web.client.RestTemplateBuilder
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
        val interceptors: List<ClientHttpRequestInterceptor> = listOf(
            BasicAuthenticationInterceptor(
                restTemplateConfig.login,
                restTemplateConfig.password
            )
        )

        restTemplateBuilder = restTemplateBuilder
            .additionalInterceptors(interceptors)
            .additionalMessageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))
        return this

    }

    fun bearerRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        val interceptors: List<ClientHttpRequestInterceptor> = listOf(HeaderRequestInterceptor(
            HttpHeaders.AUTHORIZATION,
            RestTemplateConstants.SECURITY_BEARER_WITHESPACE + restTemplateConfig.token
        ))
        restTemplateBuilder = restTemplateBuilder.additionalInterceptors(interceptors)
        return this

    }

    fun jsonAuthentRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder
    {
        restTemplateBuilder.additionalCustomizers(
            JsonAuthentCustomizer(restTemplateConfig)
        ).additionalInterceptors(
            HeaderRequestInterceptor(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
        )

        return this
    }

    fun noAuthRestTemplate(): CustomRestTemplateBuilder {
        restTemplateBuilder = restTemplateBuilder.additionalMessageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))
        return this
    }

    fun opendataRestTemplate(restTemplateConfig: RestTemplateConfig): CustomRestTemplateBuilder {
        restTemplateBuilder = restTemplateBuilder.additionalCustomizers(
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
        if (restTemplateConfig.token.isNotEmpty()) {
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
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(interceptors)
        } else {
            log.info("[CONFIG-REST] No token found: use a non-authenticated rest template")
        }
        restTemplateBuilder = restTemplateBuilder.additionalMessageConverters( StringHttpMessageConverter(StandardCharsets.UTF_8))
        return this
    }

    fun addCustomHeaders(
        restTemplateConfig: RestTemplateConfig
    ): CustomRestTemplateBuilder {



        restTemplateConfig.customHeaders.forEach { key, value ->
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(HeaderRequestInterceptor(
                key,
                value
            ))
        }
        restTemplateConfig.customQueryParam.forEach { key, value ->
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(HeaderRequestInterceptor(
                key,
                value
            ))
        }

        log.info("[CONFIG-REST] Header with name ${restTemplateConfig.customHeaders.keys}")

        restTemplateBuilder = restTemplateBuilder.errorHandler(RestTemplateResponseErrorHandler())
        restTemplateBuilder = restTemplateBuilder.additionalMessageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))

        return this

    }



    fun disableSSL() : CustomRestTemplateBuilder
    {
        val acceptingTrustStrategy =
            TrustStrategy { chain: Array<X509Certificate?>?, authType: String? -> true }
        val sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val csf = SSLConnectionSocketFactory(sslContext)
        val cm = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory( csf)
            .build();
        val httpClient = HttpClients.custom().setConnectionManager( cm).build()
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.httpClient = httpClient
        restTemplateBuilder = restTemplateBuilder.requestFactory{ r -> requestFactory }
        return this
    }

    fun build():RestTemplate = restTemplateBuilder.build()

}