package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.Dynamic

typealias InputListener = (Dynamic, ArgumentValue) -> Unit

interface DynamicView {
    val value: ArgumentValue
    val dynamic: Dynamic

    fun validate(): Boolean
    fun setInputListener(block: InputListener)
}