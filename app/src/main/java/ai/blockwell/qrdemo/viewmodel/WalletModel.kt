package ai.blockwell.qrdemo.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.BalanceChannel
import ai.blockwell.qrdemo.api.TransactionStatusChannel
import ai.blockwell.qrdemo.api.TransfersChannel
import ai.blockwell.qrdemo.data.DataStore

class WalletModel(client: ApiClient) : ViewModel() {
    val balance by lazy { BalanceChannel(client) }
    val transfers by lazy { TransfersChannel(client) }

    // Save a reference to the Lazy instance, since transfer status isn't always initialized
    private val transferStatusLazy = lazy { TransactionStatusChannel(client, DataStore.pendingTransfer) }
    val transferStatus by transferStatusLazy

    override fun onCleared() {
        Log.d("WalletModel", "Cancelling channels")
        balance.cancel()
        transfers.cancel()

        if (transferStatusLazy.isInitialized()) {
            transferStatus.cancel()
        }
    }

    fun getPendingTransfer() = DataStore.pendingTransfer
}
