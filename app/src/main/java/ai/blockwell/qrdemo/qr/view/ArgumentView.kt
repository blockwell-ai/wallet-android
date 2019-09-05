package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue

interface ArgumentView {
    val value: ArgumentValue
    val arg: Argument

    fun update(newValue: ArgumentValue)
    fun validate(): Boolean
}