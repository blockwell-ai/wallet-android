package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.api.ArgumentValue

interface ArgumentView {
    val value: ArgumentValue
    val static: Boolean

    fun validate(): Boolean
}