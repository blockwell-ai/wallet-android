package ai.blockwell.qrdemo.trainer.suggestions

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Suggestion(
        val index: Int,
        val fullText: String,
        val votes: Int
) : Parcelable {
    @IgnoredOnParcel val text: String
    @IgnoredOnParcel val tag: String

    init {
        val index = fullText.indexOf(":")
        if (index == -1 || index > 12) {
            text = fullText
            tag = ""
        } else {
            text = fullText.substring(index + 1).trim()
            tag = fullText.slice(0 until index).trim()
        }
    }

    override fun toString() = fullText
}