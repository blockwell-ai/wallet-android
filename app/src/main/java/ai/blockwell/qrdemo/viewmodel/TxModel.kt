package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.data.DataStore
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

    suspend fun getCode(url: Uri) = viewModelScope.async {
        tx.get(url.path!!, url.query ?: "")
    }

    suspend fun getCodes(codes: List<String>) = tx.getCodes(codes)

    suspend fun submitCode(url: Uri, values: Map<String, ArgumentValue> = mapOf()) = viewModelScope.async {
        tx.submit(url.path!!, url.query ?: "", values)
    }

    suspend fun createVoteQr(contractId: String, suggestionId: Int): Result<CreateQrResponse, Exception> {
        return tx.voteCode(contractId, suggestionId)
    }

    suspend fun localBitmapUri(imageUrl: String, path: File) = withContext(Dispatchers.Default) {
        var bmpUri: Uri? = null
        try {
            val image = Coil.get(imageUrl) as BitmapDrawable
            val bmp = image.bitmap
            val file = File(path, "share_image_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bmpUri
    }

    suspend fun getTxStatus(txId: String) = withContext(Dispatchers.Default) {
        client.getWithAuth("api/tokens/transactions/$txId",
                DataStore.accessToken,
                TransactionStatusResponse.Deserializer)
    }
}
