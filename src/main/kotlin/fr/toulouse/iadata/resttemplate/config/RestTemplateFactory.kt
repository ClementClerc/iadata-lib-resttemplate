package fr.toulouse.iadata.resttemplate.config

import fr.toulouse.iadata.resttemplate.beans.RestTemplateType
import fr.toulouse.iadata.resttemplate.service.CustomRestTemplateBuilder
import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Scope("prototype")
class RestTemplateFactory() {


    private val log: KLogger = KotlinLogging.logger { }

    fun createRestTemplate(restTemplateConfig: RestTemplateConfig): RestTemplate {

        val customRestTemplateBuilder = CustomRestTemplateBuilder()
        when (restTemplateConfig.restTemplateType) {
            RestTemplateType.BASIC -> {
                log.info("[CONFIG-REST] Config used: rest template with basic authentication")
                customRestTemplateBuilder.basicRestTemplate(restTemplateConfig)
            }

            RestTemplateType.BEARER -> {
                log.info("[CONFIG-REST] Config used: rest template with bearer authentication")
                customRestTemplateBuilder.bearerRestTemplate(restTemplateConfig)
            }

            RestTemplateType.JSON -> {
                log.info("[CONFIG-REST] Config used: rest template with json based authentication")
                customRestTemplateBuilder.jsonAuthentRestTemplate(restTemplateConfig)
            }

            RestTemplateType.APIDATA -> {
                log.info("[CONFIG-REST] Config used: rest template with basic authentication")
                customRestTemplateBuilder.bearerRestTemplate(restTemplateConfig)
            }

            RestTemplateType.OPENDATA -> {
                log.info("[CONFIG-REST] Config used: rest template with basic authentication")
                customRestTemplateBuilder.opendataRestTemplate(restTemplateConfig)
            }

            RestTemplateType.NONE -> {
                log.info("[CONFIG-REST] Config used: rest template with basic authentication")
                customRestTemplateBuilder.noAuthRestTemplate(restTemplateConfig)
            }

            else -> {
                log.info("[CONFIG-REST] Config used (default): rest template with basic authentication")
                log.warn(
                    "[CONFIG-REST] You need to specify explicitly in properties file which authent you want to use"
                )
                customRestTemplateBuilder.noAuthRestTemplate(restTemplateConfig)
            }
        }

        if (restTemplateConfig.proxyConfig.useProxy) {
            customRestTemplateBuilder.proxy(restTemplateConfig)
        }



        return customRestTemplateBuilder.build()
    }

}