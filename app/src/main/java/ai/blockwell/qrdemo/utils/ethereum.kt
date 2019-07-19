package ai.blockwell.qrdemo.utils

private val addressRegex = Regex("^0x[a-fA-F0-9]{40}$")

fun String?.isValidAddress(): Boolean {
    return if (this == null) {
        false
    } else {
        addressRegex.matches(this)
    }
}