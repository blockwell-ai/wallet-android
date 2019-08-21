package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.BuildConfig
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.CreateQrResponse
import ai.blockwell.qrdemo.api.LogEvent
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_suggestion_created.view.*
import org.jetbrains.anko.layoutInflater


@SuppressLint("ViewConstructor")
class SuggestionCreatedView(context: Context) : FrameLayout(context) {
    init {
        context.layoutInflater.inflate(R.layout.view_suggestion_created, this, true)
    }

    /*
    event SuggestionCreated(uint256 suggestionId, string text);
     */
    fun update(event: LogEvent, qr: CreateQrResponse?, bitmapUri: Uri?) {
        suggestion_id.text = context.getString(R.string.suggestion_id, event.returnValues["suggestionId"])

        if (qr != null) {
            view_qr.setOnClickListener {
                val webpage = Uri.parse("${BuildConfig.API_BASEURL}/code/${qr.shortcode}")
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
            share_qr.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, "QR Code to vote for my suggestion. Direct link: ${qr.url}")
                if (bitmapUri != null) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                    shareIntent.type = "image/png"
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "send"))
            }
            view_qr.visibility = View.VISIBLE
            share_qr.visibility = View.VISIBLE
        } else {
            view_qr.visibility = View.GONE
            share_qr.visibility = View.GONE
        }
    }
}
