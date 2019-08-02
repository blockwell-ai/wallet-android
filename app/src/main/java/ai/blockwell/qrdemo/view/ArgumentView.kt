package ai.blockwell.qrdemo.view

import ai.blockwell.qrdemo.api.ArgumentValue

interface ArgumentView {
    val value: ArgumentValue

    fun validate(): Boolean
}