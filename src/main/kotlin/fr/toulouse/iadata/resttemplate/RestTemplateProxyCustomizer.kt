package fr.toulouse.iadata.resttemplate

import fr.toulouse.iadata.resttemplate.config.ProxyConfig
import mu.KotlinLogging
import org.apache.http.HttpException
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.protocol.HttpContext
import org.apache.http.ssl.TrustStrategy
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
            val credentials: Credentials = UsernamePasswordCredentials(proxyUserName, proxyPassword)
            val authScope: AuthScope = AuthScope.ANY
            val credsProvider: CredentialsProvider = BasicCredentialsProvider()
            credsProvider.setCredentials(authScope, credentials)
            val proxy = HttpHost(proxyHost, proxyPort)
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider)
                .setRoutePlanner(object : DefaultProxyRoutePlanner(proxy) {
                    @Throws(HttpException::class)
                    override fun determineProxy(target: HttpHost?, request: HttpRequest?, context: HttpContext?): HttpHost {
                        return super.determineProxy(target, request, context)
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
            sslContext =
                org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        } catch (e: NoSuchAlgorithmException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        } catch (e: KeyStoreException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        } catch (e: KeyManagementException) {
            log.error("[CONFIG-REST] Error while trying to config the SSL context", e)
        }
        val csf = SSLConnectionSocketFactory(sslContext)
        val httpClient: HttpClient = httpClientBuilder.setSSLSocketFactory(csf).build()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
    }

}