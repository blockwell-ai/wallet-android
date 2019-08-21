package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.api.get
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TxModel(val client: ApiClient) : ViewModel() {
    private val tx = Tx(client)

    private var txStatus: TransactionStatusChannel? = null

    suspend fun getCode(url: Uri) = viewModelScope.async {
        tx.get(url.path!!, url.query ?: "")
    }

    suspend fun submitCode(url: Uri, values: List<ArgumentValue> = listOf()) = viewModelScope.async {
        tx.submit(url.path!!, url.query ?: "", values)
    }

    suspend fun createVoteQr(contractId: String, suggestionId: Int): Result<CreateQrResponse, Exception> {
        return tx.create(CreateQrRequest(
                contractId,
                "vote",
                listOf(
                        Argument(
                                "Suggestion",
                                "uint",
                                value = StringArgumentValue(suggestionId.toString()),
                                help = "The suggestion you're voting for.",
                                name = "suggestionId"),
                        Argument(
                                "Comment",
                                "string",
                                dynamic = "comment",
                                help = "An optional comment to include with your vote.",
                                name = "comment"
                        )
                )
        ))
    }

    suspend fun localBitmapUri(imageUrl: String, path: File) = withContext(Dispatchers.Default) {
        val image = Coil.get(imageUrl) as BitmapDrawable
        val bmp = image.bitmap
        var bmpUri: Uri? = null
        try {
            val file = File(path, "share_image_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        bmpUri
    }

    fun getTxStatus(txId: String): TransactionStatusChannel {
        val status = txStatus

        if (status != null) {
            return status
        }

        val newStatus = TransactionStatusChannel(client, txId)
        txStatus = newStatus

        return newStatus
    }

    override fun onCleared() {
        txStatus?.cancel()
    }
}
