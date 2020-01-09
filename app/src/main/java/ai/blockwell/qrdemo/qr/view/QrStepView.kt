package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.startActivity
import com.github.ajalt.timberkt.Timber
import kotlinx.android.synthetic.main.view_qr_step.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource

@SuppressLint("ViewConstructor")
class QrStepView(context: Context, val code: TxResponse, val step: Step, val votingModel: VotingModel) : FrameLayout(context) {
    private val views = mutableListOf<ArgumentView>()

    init {
        context.layoutInflater.inflate(R.layout.view_qr_step, this, true)

        function.text = step.method
        contract.text = step.address

        step.arguments.forEach {
            val view: ArgumentView = if (it.type == "suggestion" || it.type == "proposal") {
                StaticSuggestionView(context, step.contractId, it, code, votingModel)
            } else if (it.source != null && it.source.type == "json") {
                JsonArgumentView(context, it)
            } else {
                StaticArgumentView(context, it)
            }

            views.add(view)
            arguments.addView(view as View)
        }

        if (step.transactionId != null) {
            status_wrap.visibility = View.VISIBLE
        }
    }

    fun update(dynamic: Map<String, ArgumentValue>) {
        updateViews(views, dynamic)
    }

    private fun updateViews(list: List<ArgumentView>, dynamic: Map<String, ArgumentValue>) {
        list.forEach { view ->
            if (view.arg.value == null) {
                view.arg.source?.apply {
                    when (type) {
                        "dynamic" -> {
                            if (dynamic[name] != view.value) {
                                view.update(dynamic[name] ?: StringArgumentValue(""))
                            }
                        }
                        "steps" -> {
                            try {
                                val index = parameter!!.toInt() - 1
                                val step = code.steps[index]
                                val value = when (name) {
                                    "address" -> step.address
                                    else -> ""
                                }
                                view.update(StringArgumentValue(value ?: ""))
                            } catch (e: Exception) {
                                Timber.e(e) { "Exception getting steps data" }
                            }
                        }
                        "other" -> {
                            val value = when (name) {
                                "sender" -> "Your Wallet Address"
                                else -> ""
                            }
                            view.update(StringArgumentValue(value))
                        }
                        "json" -> {
                            updateViews((view as JsonArgumentView).views, dynamic)
                        }
                    }
                    Timber.d {"Updated ${view.arg.name} with ${view.value}"}
                }
            }
        }
    }

    fun updateOne(name: String, value: ArgumentValue) {
        updateOneInViews(views, name, value)
    }

    private fun updateOneInViews(list: List<ArgumentView>, name: String, value: ArgumentValue) {
        list.forEach { view ->
            val source = view.arg.source
            if (view.arg.value == null && source != null) {
                if (source.type == "json") {
                    updateOneInViews((view as JsonArgumentView).views, name, value)
                } else if (source.name == name) {
                    view.update(value)
                }
            }
        }
    }

    fun updateStatus(tx: TransactionStatusResponse) {
        tx.transactionHash?.let { hash ->
            etherscan.visibility = View.VISIBLE
            etherscan.textColorResource = R.color.link
            etherscan.setOnClickListener {
                val webpage = Uri.parse(Etherscan.tx(tx.network, hash))
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        }
        when (tx.status) {
            "completed" -> {
                status.setText(R.string.success)
                status.textColorResource = R.color.success
                progress.visibility = View.GONE
                checkmark.visibility = View.VISIBLE
            }
            "error" -> {
                statusError()
            }
        }
    }

    fun statusError() {
        status.setText(R.string.error)
        status.textColorResource = R.color.error
        progress.visibility = View.GONE
    }
}
