package ai.blockwell.qrdemo.trainer.suggestions

data class Suggestion(
        val index: Int,
        val text: String,
        val votes: Int
) {
    override fun toString() = text
}