package fr.toulouse.iadata.resttemplate

import fr.toulouse.iadata.resttemplate.config.ProxyConfig
import fr.toulouse.iadata.resttemplate.config.RestTemplateFactoryBuilder
import fr.toulouse.iadata.resttemplate.config.TestConfig
import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles

@SpringBootTest( classes =  [TestConfig::class,RestTemplateProperties::class,RestTemplateFactoryBuilder::class] )
@ActiveProfiles("test")
class RestTemplateProxyCustomizerTest (@Autowired private var restTemplateProperties: RestTemplateProperties){
    @Test
    fun testLoadProperties(){
        val proxyConfig = ProxyConfig(useProxy = true, proxyHostEnvName = "PROXY_HOST", proxyPortEnvName = "PROXY_PORT", proxyUserNameEnvName = "PROXY_USERNAME", proxyPasswordEnvName = "PROXY_PASSWORD")
        Assertions.assertEquals(restTemplateProperties.restTemplateConfigs.get(0).proxyConfig,proxyConfig)
    }
}