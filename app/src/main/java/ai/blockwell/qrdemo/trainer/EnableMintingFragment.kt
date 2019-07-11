package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_enable_minting.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.koin.android.architecture.ext.sharedViewModel
import org.koin.android.ext.android.inject

class EnableMintingFragment : Fragment() {
    val scope = MainScope()
    val model by sharedViewModel<TrainerModel>()
    val client by inject<ApiClient>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enable_minting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipient.setText(DataStore.accountAddress)

        enable_minting.setOnClickListener { enableMinting() }
        mint.setOnClickListener { mintTokens() }
    }

    override fun onResume() {
        super.onResume()
        getMintingStatus()
    }

    fun getMintingStatus() {
        scope.launch {
            val result = model.getMintingStatus()
            result.component1()?.let {
                if (it.data.asBoolean) {
                    minting_status.setText(R.string.enabled)
                } else {
                    minting_status.setText(R.string.disabled)
                }
            }
        }
    }

    fun enableMinting() {
        enable_minting.isEnabled = false

        scope.launch {
            val result = model.enableMinting()
            val tx = result.component1()

            if (tx != null) {
                watchTransaction(tx.id, R.string.enabling_minting, R.string.minting_enabled) {
                    getMintingStatus()
                }
            } else {
                enable_minting.isEnabled = true
            }
        }
    }

    fun mintTokens() {
        val rec = recipient.text.toString()
        val value = value.text.toString().fromDecimals(18)

        if (value.isEmpty()) {
            value_layout.error = getString(R.string.enter_valid_amount)
            return
        }

        mint.isEnabled = false
        scope.launch {
            val result = model.mintTokens(rec, value)
            val tx = result.component1()

            if (tx != null) {
                watchTransaction(tx.id, R.string.minting_tokens, R.string.tokens_minted) {
                    mint.isEnabled = true
                }
            } else {
                enable_minting.isEnabled = true
            }
        }
    }

    fun watchTransaction(id: String, pendingText: Int, successText: Int, completed: (TransactionStatusResponse) -> Unit) {
        scope.launch {
            val snackbar = Snackbar
                    .make(enable_minting, pendingText, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()

            val channel = TransactionStatusChannel(client, id)

            channel.channel.consumeEach {
                if (it.status == "completed") {
                    snackbar.dismiss()
                    val snackbarSuccess = Snackbar
                            .make(enable_minting, successText, Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(getColor(requireContext(), R.color.link))

                    snackbarSuccess.setAction(R.string.view_on_etherscan) { _ ->
                        val webpage = Uri.parse(Etherscan.tx(it.network, it.transactionHash!!))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(requireActivity().packageManager) != null) {
                            startActivity(intent)
                        }
                        snackbarSuccess.dismiss()
                    }
                    snackbarSuccess.show()
                    completed(it)
                } else if (it.status == "error") {
                    snackbar.dismiss()
                    requireActivity().alert(R.string.contract_send_failed).show()
                    completed(it)
                }
            }
        }
    }
}
