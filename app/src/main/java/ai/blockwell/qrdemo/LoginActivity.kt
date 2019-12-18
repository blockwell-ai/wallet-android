package ai.blockwell.qrdemo

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ai.blockwell.qrdemo.api.Auth
import ai.blockwell.qrdemo.api.Tokens
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.qr.TxActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity() {

    val auth: Auth by inject()
    val tokens : Tokens by inject()

    var deepLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        setTitle(R.string.login)

        deepLink = intent.getStringExtra("deepLink")

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in.setOnClickListener { attemptLogin() }
        register.setOnClickListener { startActivity<RegisterActivity>("deepLink" to deepLink) }

        if (auth.getEmail().isNotEmpty()) {
            email.setText(auth.getEmail())
            password.requestFocus()
        }
    }

    fun attemptLogin() {
        val emailInput = email.text.toString()
        val passwordInput = password.text.toString()

        if (emailInput.isEmpty()) {
            alert(R.string.please_enter_email).show()
            return
        }

        if (passwordInput.isEmpty()) {
            alert(R.string.please_enter_password).show()
            return
        }

        login_progress.visibility = View.VISIBLE
        sign_in.isEnabled = false


        // Launch a coroutine for the background work
        scope.launch {
            val result = auth.login(emailInput, passwordInput)

            result.fold({
                if ((BuildConfig.APP_ID == "base" || BuildConfig.APP_ID == "blockwell") && it.customToken != null) {
                    tokens.get(it.customToken)
                    DataStore.suggestionsToken = it.customToken
                }

                val link = deepLink

                if (link != null) {
                    startActivity(intentFor<TxActivity>("url" to link).clearTask().newTask())
                } else {
                    startActivity(intentFor<WalletActivity>().clearTask().newTask())
                }
                finish()
            }, { error ->
                val message = error.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })

            login_progress.visibility = View.INVISIBLE
            sign_in.isEnabled = true
        }
    }
}
