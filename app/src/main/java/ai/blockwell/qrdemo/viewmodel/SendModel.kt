package ai.blockwell.qrdemo.viewmodel

import androidx.lifecycle.ViewModel
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.TransferTokensChannel

class SendModel(client: ApiClient) : ViewModel() {
    val tokens by lazy { TransferTokensChannel(client) }

    override fun onCleared() {
        tokens.cancel()
    }
}
