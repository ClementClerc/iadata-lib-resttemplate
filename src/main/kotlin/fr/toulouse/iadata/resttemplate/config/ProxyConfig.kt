package fr.toulouse.iadata.resttemplate.config


data class ProxyConfig(
    var useProxy: Boolean = false,
    var proxyUserNameEnvName : String = "",
    var proxyPasswordEnvName : String = "",
    var proxyHostEnvName : String = "",
    var proxyPortEnvName : String = ""
) {
}