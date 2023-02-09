package fr.toulouse.iadata.resttemplate

import fr.toulouse.iadata.resttemplate.config.ProxyConfig
import fr.toulouse.iadata.resttemplate.config.RestTemplateFactory
import fr.toulouse.iadata.resttemplate.config.TestConfig
import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.Assert
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest( classes =  [TestConfig::class] )
@ActiveProfiles("test")
class RestTemplateProxyCustomizerTest(
    @Autowired private var restTemplateProperties: RestTemplateProperties,
//    @Autowired private var context: GenericWebApplicationContext? ,
    @Autowired private var restTemplateFactory: RestTemplateFactory,
    @Autowired @Qualifier("restTemplate1") private var restTemplate1: RestTemplate,
    @Autowired @Qualifier("restTemplate2") private var restTemplate2: RestTemplate,
    @Autowired @Qualifier("restTemplate3") private var restTemplate3: RestTemplate,
    @Autowired @Qualifier("restTemplate4") private var restTemplate4: RestTemplate
){

    @Test
    fun testLoadProperties(){
        val proxyConfig = ProxyConfig(useProxy = true, proxyHostEnvName = "PROXY_HOST", proxyPortEnvName = "PROXY_PORT", proxyUserNameEnvName = "PROXY_USERNAME", proxyPasswordEnvName = "PROXY_PASSWORD")
        Assertions.assertEquals(restTemplateProperties.restTemplateConfigs.get(0).proxyConfig,proxyConfig)
    }

    @Test
    fun testCallInternalURLWithUnauthorized(){
        try {
            restTemplate3.getForEntity(
                UriComponentsBuilder
                    .fromHttpUrl("https://iadata-dev-ingest.applis.intra:10443")
                    .build()
                    .toUri(),
                String::class.java)
        }
        catch ( e : Exception )
        {
            if ( e !is HttpClientErrorException.Unauthorized)
            {
                throw e
            }
        }
    }

    @Test
    fun testCallInternalURLWithSuccess(){
        val response = restTemplate4.getForEntity(
                UriComponentsBuilder
                    .fromHttpUrl("https://iadata-dev-ingest.applis.intra:10443")
                    .build()
                    .toUri(),
                String::class.java)

        Assertions.assertTrue( response.statusCode.is2xxSuccessful )
    }



    @Test
    fun testGetMultipleBeans(){
        val response : ResponseEntity<String> = restTemplate1.getForEntity(
            UriComponentsBuilder
                .fromHttpUrl("https://google.com")
                .build()
                .toUri(),
            String::class.java)

        val response2 : ResponseEntity<String> = restTemplate2.getForEntity(
            UriComponentsBuilder
                .fromHttpUrl("https://google.com")
                .build()
                .toUri(),
            String::class.java)
        Assertions.assertEquals(HttpStatus.OK,response.statusCode)
        Assertions.assertEquals(HttpStatus.OK,response2.statusCode)
    }

    @Test
    fun testBeanFactory(){
        val restTemplate1 : RestTemplate = restTemplateFactory.createRestTemplate(restTemplateProperties.restTemplateConfigs.get(0))
        val response : ResponseEntity<String> = restTemplate1.getForEntity(
            UriComponentsBuilder
                .fromHttpUrl("https://google.com")
                .build()
                .toUri(),
            String::class.java)
        Assertions.assertEquals(HttpStatus.OK,response.statusCode)
    }
}