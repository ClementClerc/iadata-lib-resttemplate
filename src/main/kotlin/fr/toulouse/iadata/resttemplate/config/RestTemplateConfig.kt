package fr.toulouse.iadata.resttemplate.config

import fr.toulouse.iadata.resttemplate.beans.CookieReturnType
import fr.toulouse.iadata.resttemplate.beans.RestTemplateType
import org.springframework.stereotype.Component

@Component
data class RestTemplateConfig (
    var restTemplateName : String = "",
    var proxyConfig : ProxyConfig = ProxyConfig(),
    var restTemplateType : RestTemplateType = RestTemplateType.NONE,
    var login : String = "",
    var password : String = "",
    var token : String = "",
    var url : String = "",
    var loginRawData : String = "",
    var cookieReturnType : CookieReturnType = CookieReturnType.NONE,
    var cookieLoginName : String = "",
    var incomingCookieName : String = "",
    var cookieLoginPrefix : String = "",
    var authorizationHeaderName : String = "",
    var customHeaders : Map<String,String> = emptyMap(),
    var customQueryParam : Map<String,String> = emptyMap(),
    var body : String = "",
    var disableSSL : Boolean = false

){
}