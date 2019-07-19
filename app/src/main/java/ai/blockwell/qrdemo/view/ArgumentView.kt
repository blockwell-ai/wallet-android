package ai.blockwell.qrdemo.view

interface ArgumentView {
    val value: String

    fun validate(): Boolean
}