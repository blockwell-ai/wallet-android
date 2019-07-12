package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.fromDecimals
import ai.blockwell.qrdemo.data.DataStore
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_enable_minting.*
import kotlinx.coroutines.launch

class EnableMintingFragment : GuidedFragment() {
    // Standard Android method to create layout from XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enable_minting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setting the default recipient as the user's wallet address
        recipient.setText(DataStore.accountAddress)

        // Click listeners on the buttons
        enable_minting.setOnClickListener { enableMinting() }
        mint.setOnClickListener { mintTokens() }
    }

    override fun onResume() {
        super.onResume()

        // Refresh minting status when this fragment resumes
        getMintingStatus()
    }

    // This function checks if minting is enabled on chain and displays that
    fun getMintingStatus() {
        // scope is for coroutines
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
        // This disables the submit button so they can't double send
        enable_minting.isEnabled = false

        scope.launch {
            val result = model.enableMinting()
            val tx = result.component1()

            if (tx != null) {
                // watchTransaction is defined in GuidedFragment
                watchTransaction(enable_minting, tx.id, R.string.enabling_minting, R.string.minting_enabled) {
                    getMintingStatus()
                }
            } else {
                enable_minting.isEnabled = true
            }
        }
    }

    fun mintTokens() {
        // Get the values from the form fields
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
                // watchTransaction is defined in GuidedFragment
                watchTransaction(mint, tx.id, R.string.minting_tokens, R.string.tokens_minted) {
                    mint.isEnabled = true
                }
            } else {
                mint.isEnabled = true
            }
        }
    }
}
