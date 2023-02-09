package fr.toulouse.iadata.resttemplate

import fr.toulouse.iadata.resttemplate.config.ProxyConfig
import mu.KotlinLogging
import org.apache.hc.client5.http.auth.*
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.core5.http.HttpException
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.http.HttpRequest
import org.apache.hc.core5.http.protocol.HttpContext
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.ssl.TrustStrategy
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext

class RestTemplateProxyCustomizer(private val proxyConfig: ProxyConfig) : RestTemplateCustomizer{

    private val log = KotlinLogging.logger("")


    override fun customize(restTemplate: RestTemplate) {
        log.info("[CONFIG-REST] Customize the rest template with addition of the proxy")
        val httpClientBuilder : HttpClientBuilder = HttpClientBuilder.create()
        if (proxyConfig.useProxy) {
            log.info("[CONFIG-REST] The data fetcher is set for being used behind proxy.")
            var hasErrorOnConfig = false
            val proxyUserName = System.getenv(proxyConfig.proxyUserNameEnvName)
            val proxyPassword = System.getenv(proxyConfig.proxyPasswordEnvName)
            val proxyHost = System.getenv(proxyConfig.proxyHostEnvName)
            var proxyPort = 80
            try {
                proxyPort = System.getenv(proxyConfig.proxyPortEnvName).toInt()
            } catch (e: Exception ) {
                hasErrorOnConfig = true
                log.error("[CONFIG-REST] Unable to parse proxy port to integer", e)
            }
            if (proxyHost == null || proxyPassword == null || proxyUserName == null) {
                hasErrorOnConfig = true
            }
            if (hasErrorOnConfig) {
                log.error("[CONFIG-REST] Error while retryving infos for the auth of the proxy")
                return
                // TODO : Make the proxy customizer available for non auth proxies.
            }
            val credentials: Credentials = UsernamePasswordCredentials(proxyUserName, proxyPassword.toCharArray())
            var credsProvider: CredentialsStore = BasicCredentialsProvider()
            credsProvider.setCredentials( AuthScope( HttpHost( "*")), credentials )
            val proxy = HttpHost(proxyHost, proxyPort)
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider)
                .setRoutePlanner(object : DefaultProxyRoutePlanner(proxy) {
                    @Throws(HttpException::class)
                    override fun determineProxy(target: HttpHost?, context: HttpContext?): HttpHost {
                        return super.determineProxy(target, context)
                    }
                })
        } else {
            log.info("[CONFIG-REST] The data fetcher is set for being used with no proxy.")
        }
        log.info("[CONFIG-REST] Set the SSL context")
        val acceptingTrustStrategy =
            TrustStrategy { chain: Array<X509Certificate?>?, authType: String? -> true }
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        } catch (e: NoSuchAlgorithmException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        } catch (e: KeyStoreException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        } catch (e: KeyManagementException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        }
        val csf = SSLConnectionSocketFactory(sslContext)

        val cm = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory( csf)
            .build();
        val httpClient = HttpClients.custom().setConnectionManager( cm).build()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
    }

}