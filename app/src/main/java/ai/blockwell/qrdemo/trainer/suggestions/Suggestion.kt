package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.api.gson
import android.os.Parcelable
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Suggestion(
        val id: Int = -1,
        val text: Map<String, String> = mapOf(),
        val votes: String = "0",
        val proposal: Boolean = false
) : Parcelable {

    companion object {
        fun parse(id: Int, text: String, votes: String, proposal: Boolean = false): Suggestion {
            val map: Map<String, String> = if (text.startsWith("{")) {
                try {
                    val token = object : TypeToken<Map<String, String>>() { }.type
                    gson.fromJson<Map<String, String>>(text, token)
                } catch (e: JsonSyntaxException) {
                    // Ignore
                    mapOf(
                            "suggestion" to text
                    )
                }
            } else {
                val colon = text.indexOf(":")
                if (colon > -1 && colon < 13) {
                    mapOf(
                            "tag" to text.slice(0 until colon),
                            "suggestion" to text.slice(colon + 1 until text.length)
                    )
                } else {
                    mapOf("suggestion" to text)
                }
            }

            return Suggestion(id, map, votes, proposal)
        }
    }

    @IgnoredOnParcel
    val suggestion: String by lazy {
        val suggestion = text["suggestion"]
        when {
            suggestion != null -> suggestion
            text.size == 1 -> text.values.first()
            else -> text.filterKeys { it != "tag" }.values.first()
        }
    }

    @IgnoredOnParcel
    val tag: String
        get() {
            return text["tag"] ?: ""
        }
}

enum class SuggestionType {
    ALL,
    SUGGESTION,
    PROPOSAL
}
