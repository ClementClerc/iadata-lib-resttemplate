package fr.toulouse.iadata.resttemplate.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import fr.toulouse.iadata.resttemplate.beans.CookieReturnType
import fr.toulouse.iadata.resttemplate.beans.HeaderRequestInterceptor
import fr.toulouse.iadata.resttemplate.exception.WrongConfigurationException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

class JsonAuthentCustomizer(val restTemplateConfig : RestTemplateConfig) : RestTemplateCustomizer {

    private val sleepingThreadInMin = 1

    private val log : KLogger = KotlinLogging.logger("")

    override fun customize(restTemplate: RestTemplate) {
        log.info("[CONFIG-REST] Customize the rest template with addition of auth cookie")
        log.info("[CONFIG-REST] First, process authentication to get the auth cookie")
        var retry = true
        var retryNumber = 0
        while (retry && retryNumber < 5) {
            try {
                val jsonContent: String = restTemplateConfig.loginRawData!!
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                val response: ResponseEntity<String> = restTemplate.exchange(
                    restTemplateConfig.url,
                    HttpMethod.POST, HttpEntity(jsonContent, headers),
                    String::class.java
                )
                var authCookieValue: String? = ""
                if (!restTemplateConfig.cookieReturnType.equals("")) {
                    when (restTemplateConfig.cookieReturnType) {
                        CookieReturnType.BODY -> authCookieValue = getCookieFromBody(response)
                        CookieReturnType.HEADERS -> authCookieValue = getSetCookie(response, restTemplateConfig.incomingCookieName)
                        else -> {}
                    }

                    if (!restTemplateConfig.cookieLoginPrefix.equals("")) {
                        authCookieValue = restTemplateConfig.cookieLoginPrefix + authCookieValue
                    }
                    val listInterceptors = restTemplate.interceptors

                    if (authCookieValue != null) {
                        listInterceptors.add(
                            HeaderRequestInterceptor(
                                restTemplateConfig.cookieLoginName,
                                authCookieValue
                            )
                        )
                    }
                    restTemplate.interceptors = listInterceptors
                    retry = false
                }else{
                    throw WrongConfigurationException("[CONFIG-REST] No cookie return type set")
                }
            } catch (e: HttpClientErrorException) {
                log.warn("[CONFIG-REST] Authentication failed, number of retry : {} error :", retryNumber, e)
                retryNumber += 1
                try {
                    //wait 4 minutes
                    log.warn("[CONFIG-REST] Wait for {} minutes", sleepingThreadInMin)
                    Thread.sleep(sleepingThreadInMin.toLong() * 60000)
                } catch (ex: InterruptedException) {
                    log.error("[CONFIG-REST] Error while trying to put the current thread to sleep", ex)
                    Thread.currentThread().interrupt()
                }
            } catch (e: ResourceAccessException) {
                log.warn("[CONFIG-REST] Authentication failed, number of retry : {} error :", retryNumber, e)
                retryNumber += 1
                try {
                    log.warn("[CONFIG-REST] Wait for {} minutes", sleepingThreadInMin)
                    Thread.sleep(sleepingThreadInMin.toLong() * 60000)
                } catch (ex: InterruptedException) {
                    log.error("[CONFIG-REST] Error while trying to put the current thread to sleep", ex)
                    Thread.currentThread().interrupt()
                }
            }
        }
        if (retryNumber >= 5 && retry == true) {
            log.warn("[CONFIG-REST] Authentication failed, number of retry : {} error :", retryNumber)
        }
    }

    private fun getSetCookie(response: ResponseEntity<String>, headerName: String?): String? {
        var headerName: String = headerName ?: "Set-Cookie"

        val listCookies = response.headers[headerName]
        if (listCookies != null) {
            for (cookie in listCookies) {
                val splitCookie = cookie.split("=".toRegex(), limit = 2).toTypedArray()
                if (splitCookie.size == 2) {
                    if (splitCookie[0] == restTemplateConfig.cookieLoginName) {
                        log.info("[CONFIG-REST] Auth cookie successful")
                        return cookie
                    }
                }
            }
        }
        return null
    }

    private fun getCookieFromBody(response: ResponseEntity<String>): String? {
        try {
            val body =
                ObjectMapper().readTree(response.body)
            var cookieName : String ?= restTemplateConfig?.incomingCookieName
            return body.get(cookieName).asText()
        } catch (e: JsonProcessingException) {
            log.error{ e }
        }
        return null

    }
}