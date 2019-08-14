package ai.blockwell.qrdemo.trainer.suggestions

data class Suggestion(
        val index: Int,
        val fullText: String,
        val votes: Int
) {
    val text: String
    val tag: String

    init {
        val index = fullText.indexOf(":")
        if (index == -1) {
            text = fullText
            tag = ""
        } else {
            text = fullText.substring(index + 1).trim()
            tag = fullText.slice(0 until index).trim()
        }
    }

    override fun toString() = fullText
}