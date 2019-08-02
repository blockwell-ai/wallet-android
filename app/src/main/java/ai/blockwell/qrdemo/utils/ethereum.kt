package ai.blockwell.qrdemo.utils

import ai.blockwell.qrdemo.api.ArgumentValue

private val addressRegex = Regex("^0x[a-fA-F0-9]{40}$")

fun String?.isValidAddress(): Boolean {
    return if (this == null) {
        false
    } else {
        addressRegex.matches(this)
    }
}

fun ArgumentValue?.isValidAddress(): Boolean {
    if (this == null) {
        return false
    }

    if (this.isArray()) {
        this.getArray().forEach {
            if (!it.isValidAddress()) {
                return false
            }
        }
    } else {
        return this.getValue().isValidAddress()
    }

    return true
}