package ai.blockwell.qrdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import ai.blockwell.qrdemo.data.DataStore
import kotlinx.android.synthetic.main.activity_qr.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.displayMetrics
import kotlin.math.roundToInt

/**
 * Shows a QR code of the user's Ethereum address.
 */
public class QrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        setSupportActionBar(toolbar)
        account_address.text = DataStore.accountAddress

        val size = (displayMetrics.widthPixels * 0.75).roundToInt()
        val bitmap = QRCode.from(DataStore.accountAddress)
                .withColor(ContextCompat.getColor(this, R.color.colorTextEmphasis), ContextCompat.getColor(this, R.color.contentBackground))
                .withSize(size, size)
                .bitmap()
        qr_image.setImageBitmap(bitmap)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
