package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.*
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.qr.view.*
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.viewmodel.TxModel
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_tx.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxActivity : AppCompatActivity() {
    val scope = MainScope()
    val model by viewModel<TxModel>()
    val votingModel by viewModel<VotingModel>()
    val auth: Auth by inject()
    lateinit var url: Uri

    var stepViews: List<QrStepView> = listOf()
    var dynamicViews: List<DynamicView> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (intent.hasExtra("url")) {
            url = Uri.parse(intent.getStringExtra("url"))
        } else if (data != null) {
            url = data
        }

        if (!auth.isLoggedIn()) {
            startActivity<LoginActivity>("deepLink" to url.toString())
            finish()
            return
        }

        setContentView(R.layout.activity_tx)
        title = "Transaction"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accept.setOnClickListener { submit() }
        cancel.setOnClickListener { finish() }
        collapsing_title.setOnClickListener {
            if (collapsing.visibility == View.GONE) {
                collapsing.visibility = View.VISIBLE
                collapsing_arrow.toggle()
            } else {
                collapsing.visibility = View.GONE
                collapsing_arrow.toggle()
            }
        }

        load()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SuggestionsActivity.REQUEST -> if (resultCode == Activity.RESULT_OK && data != null) {
                val name = data.getStringExtra("name")
                val dynamicView = dynamicViews.find { it.dynamic.name == name }
                val suggestion = data.getParcelableExtra<Suggestion>("suggestion")
                if (name != null && dynamicView != null && suggestion != null) {
                    if (dynamicView is InputSuggestionView) {
                        dynamicView.setSuggestion(suggestion)
                    }
                }
            }
            ScanQrActivity.REQUEST_CODE -> if (resultCode == Activity.RESULT_OK && data != null) {
                val name = data.getStringExtra("name")
                val dynamicView = dynamicViews.find { it.dynamic.name == name }
                if (name != null && dynamicView != null) {
                    if (dynamicView is InputArgumentView) {
                        dynamicView.setValue(data.getStringExtra("address"))
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun load() {
        scope.launch {
            val result = model.getCode(url).await()
            preview.removeAllViews()
            input_arguments.removeAllViews()

            result.fold({
                render(it)
            }, {
                val message = it.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })
        }
    }

    private fun render(tx: TxResponse) {
        if (tx.description != null) {
            description.text = tx.description
        } else {
            description.visibility = View.GONE
        }

        if (tx.title != null) {
            code_title.text = tx.title
        } else {
            code_title.visibility = View.GONE
        }

        stepViews = tx.steps.mapIndexed { index, step ->
            val view = QrStepView(this, tx, step, votingModel)
            view.update(mapOf())
            preview.addView(view)
            view.backgroundResource = when (index) {
                0 -> R.drawable.bg_func1
                1 -> R.drawable.bg_func2
                2 -> R.drawable.bg_func2
                3 -> R.drawable.bg_func2
                else -> R.drawable.bg_func1
            }

            view
        }

        dynamicViews = tx.dynamic.map {
            val view: DynamicView = if (it.type == "suggestion") {
                val view = InputSuggestionView(this, it, tx, votingModel)
                view.setOnClickListener { _ -> votingClick(it) }
                view
            } else {
                val view = InputArgumentView(this, it)
                view.qrListener = { qrClick(it) }
                view
            }
            input_arguments.addView(view as View)

            view.setInputListener { dynamic, value ->
                updatePreview(dynamic, value)
            }

            view
        }

        if (stepViews.size > 1) {
            multiple_steps.visibility = View.VISIBLE
        }
        if (dynamicViews.isEmpty()) {
            input_arguments.visibility = View.GONE
        }
    }

    private fun updatePreview(dynamic: Dynamic, value: ArgumentValue) {
        for (view in stepViews) {
            view.updateOne(dynamic.name, value)
        }
    }

    private fun votingClick(dynamic: Dynamic) {
        startActivityForResult<SuggestionsActivity>(SuggestionsActivity.REQUEST,
                "name" to dynamic.name,
                "contractId" to dynamic.contractId
        )
    }

    private fun qrClick(dynamic: Dynamic) {
        startActivityForResult<AddressQrActivity>(ScanQrActivity.REQUEST_CODE,
                "name" to dynamic.name
        )
    }

    private fun submit() {
        scope.launch {
            if (!dynamicViews.map { it.validate() }.contains(false)) {
                accept.isEnabled = false
                cancel.isEnabled = false
                progress.visibility = View.VISIBLE
                accept.text = ""

                val values = dynamicViews.map { it.dynamic.name to it.value }.toMap()
                val result = model.submitCode(url, values).await()

                result.fold({
                    val link = it.confirmationLink
                    if (!link.isNullOrEmpty()) {
                        startActivity<WebViewActivity>("title" to it.creator, "url" to link)
                    } else {
                        startActivity<TxSuccessActivity>("tx" to it)
                    }
                    finish()
                }, {
                    accept.isEnabled = true
                    cancel.isEnabled = true
                    accept.setText(R.string.accept)
                    progress.visibility = View.GONE
                    longToast(R.string.submit_failed)
                })
            }
        }
    }
}
