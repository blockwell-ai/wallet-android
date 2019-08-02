package ai.blockwell.qrdemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.TransactionStatusChannel
import ai.blockwell.qrdemo.api.Tx
import ai.blockwell.qrdemo.data.DataStore
import kotlinx.coroutines.async

class TxModel(val client: ApiClient) : ViewModel() {
    private val tx = Tx(client)

    private var txStatus: TransactionStatusChannel? = null

    suspend fun getCode(url: Uri) = viewModelScope.async {
        tx.get(url.path!!, url.query ?: "")
    }

    suspend fun submitCode(url: Uri, values: List<ArgumentValue> = listOf()) = viewModelScope.async {
        tx.submit(url.path!!, url.query ?: "", values)
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
