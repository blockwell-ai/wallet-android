package ai.blockwell.qrdemo.trainer.freeze

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TransactionStatusResponse
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import ai.blockwell.qrdemo.utils.hideKeyboard
import android.content.ClipData
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_freeze_step2.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.longToast

class Step2Fragment : StepFragment() {
    override val layoutRes = R.layout.fragment_freeze_step2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons.useSkip()

        user_wallet.setOnClickListener {
            val clipData = ClipData.newPlainText("Wallet address", user_wallet.text)

            activity?.apply {
                clipboardManager.primaryClip = clipData
                longToast(R.string.account_copied)
            }
        }
    }

    override fun submit() {
        val address = account.text.toString()

        if (address.isEmpty()) {
            account.error = "Please enter an address"
            return
        }

        submit.isEnabled = false
        hideKeyboard()
        scope.launch {
            val result = model.send("freeze", listOf(address))

            result.fold({
                watchTransaction(it.id,
                        "Freezing wallet...",
                        "Wallet frozen.") {
                    onResult(it)
                }
            }, {
                requireActivity().alert(R.string.unknown_error).show()
                submit.isEnabled = true
            })
        }
    }

    fun onResult(result: TransactionStatusResponse) {
        scope.launch {
            if (result.status == "completed") {
                model.events.publish(Events.Type.NEXT)
            } else {
                requireActivity().alert(R.string.contract_send_failed).show()
            }
        }
    }
}
