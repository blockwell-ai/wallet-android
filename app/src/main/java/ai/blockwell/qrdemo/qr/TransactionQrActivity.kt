package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.ScanQrActivity
import android.os.Bundle
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scan_qr.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import java.net.URL

class TransactionQrActivity : ScanQrActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.transaction_scan)
        description.setText(R.string.transaction_scan_description)
    }

    override fun decodeCallback(result: Result) {
        val url = URL(result.text)

        if ((url.protocol == "http" || url.protocol == "https")
                && url.host == "qr.blockwell.ai"
                ) {
            startActivity<TxActivity>("url" to result.text)
            finish()
        } else {
            longToast(R.string.qr_not_valid)
        }
    }
}