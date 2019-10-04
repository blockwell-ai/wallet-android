package ai.blockwell.qrdemo.api

object Etherscan {
    const val ETHERSCAN_MAINNET = "https://etherscan.io"
    const val ETHERSCAN_RINKEBY = "https://rinkeby.etherscan.io"

    fun tx(network: String?, hash: String) = "${baseUrl(network)}/tx/$hash"

    fun token(network: String?, address: String) = "${baseUrl(network)}/token/$address"

    fun wallet(network: String?, address: String) = "${baseUrl(network)}/address/$address"

    fun baseUrl(network: String?) = when (network) {
        "main" -> ETHERSCAN_MAINNET
        "rinkeby" -> ETHERSCAN_RINKEBY
        else -> ETHERSCAN_MAINNET
    }
}