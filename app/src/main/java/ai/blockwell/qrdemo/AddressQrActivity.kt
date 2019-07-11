package ai.blockwell.qrdemo

import android.app.Activity
import android.content.Intent
import com.google.zxing.Result
import org.jetbrains.anko.longToast

class AddressQrActivity : ScanQrActivity() {
    override fun decodeCallback(result: Result) {
        // Only accept the scan if it's an Ethereum address
        if (ETH_REGEX.matches(result.text)) {
            val resultIntent = Intent()
            resultIntent.putExtra("address", result.text)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            longToast(R.string.qr_not_address)
        }
    }
}