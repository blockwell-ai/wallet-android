package ai.blockwell.qrdemo

import android.app.Activity
import android.content.Intent
import com.google.zxing.Result
import org.jetbrains.anko.longToast

class AddressQrActivity : ScanQrActivity() {
    override fun decodeCallback(result: Result) {

        val match = ETH_PREFIXED_REGEX.find(result.text)
        val address = when {
            match != null -> match.groupValues[1]
            ETH_REGEX.matches(result.text) -> result.text
            else -> null
        }

        if (address != null) {
            val resultIntent = Intent()
            resultIntent.putExtra("address", address)

            if (intent.hasExtra("name")) {
                resultIntent.putExtra("name", intent.getStringExtra("name"))
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            longToast(R.string.qr_not_address)
            codeScanner?.startPreview()
        }
    }
}