package ai.blockwell.qrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ai.blockwell.qrdemo.api.Auth
import ai.blockwell.qrdemo.api.Tokens
import ai.blockwell.qrdemo.data.DataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject

/**
 * The launch Activity simply redirects the user to the appropriate screen.
 */
class MainActivity : AppCompatActivity() {
    val auth : Auth by inject()
    val tokens : Tokens by inject()
    val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DataStore.tokenName.isEmpty()) {
            scope.launch {
                tokens.get()
                redirect()
            }
        } else {
            redirect()
        }

    }

    private fun redirect() {
        if (auth.isLoggedIn()) {
            startActivity<WalletActivity>()
        } else {
            startActivity<LoginActivity>()
        }
        finish()
    }
}
