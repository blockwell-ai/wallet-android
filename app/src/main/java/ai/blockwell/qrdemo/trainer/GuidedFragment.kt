package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.Etherscan
import ai.blockwell.qrdemo.api.TransactionStatusChannel
import ai.blockwell.qrdemo.api.TransactionStatusResponse
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.applyRecursively
import org.koin.android.architecture.ext.sharedViewModel
import org.koin.android.ext.android.inject

abstract class GuidedFragment : Fragment() {

    // Coroutine scope for the Main thread
    val scope = MainScope()

    // Model that has data and logic for the Trainer screens
    val model by sharedViewModel<TrainerModel>()
    val client by inject<ApiClient>()

    lateinit var layout: ViewGroup

    abstract val layoutRes: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutRes, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view.findViewById<ViewGroup>(R.id.layout) == null) {
            throw Exception("GuidedFragment subclass must have R.id.layout as a ViewGroup")
        }

        layout = view.findViewById(R.id.layout)

        layout.applyRecursively {
            if (it is EditText && it.imeOptions == EditorInfo.IME_ACTION_GO) {
                it.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        submit()
                        true
                    } else {
                        false
                    }
                })
            }
        }

        view.findViewById<Button>(R.id.submit)?.setOnClickListener { submit() }

        view.findViewById<BackNextView>(R.id.buttons)?.apply {
            onNextClick {
                model.events.publish(Events.Type.NEXT)
            }
            onBackClick {
                model.events.publish(Events.Type.BACK)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (this is Events.Subscriber) {
            model.events.subscribe(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this is Events.Subscriber) {
            model.events.unsubscribe(this)
        }
    }

    open fun submit() {}

    // This function displays a snackbar for the pending transaction, and a new
    // snackbar once it completes with an Etherscan link
    fun watchTransaction(id: String, pendingText: String, successText: String, completed: (TransactionStatusResponse) -> Unit) {
        scope.launch {
            val snackbar = Snackbar
                    .make(layout, pendingText, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()

            // This is a coroutine channel that sends updates on a transaction
            val channel = TransactionStatusChannel(client, id)

            // This runs every time there's updates to the transaction
            channel.channel.consumeEach {
                if (it.status == "completed") {
                    // If it's completed, remove the pending snackbar and show the completion one
                    snackbar.dismiss()
                    val snackbarSuccess = Snackbar
                            .make(layout, successText, Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.link))

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
                    // If it errored, show that as an alert
                    snackbar.dismiss()
                    requireActivity().alert(R.string.contract_send_failed).show()
                    completed(it)
                }
            }
        }
    }

    fun watchTransaction(id: String, pendingText: Int, successText: Int, completed: (TransactionStatusResponse) -> Unit) = watchTransaction(id, getString(pendingText), getString(successText), completed)
}