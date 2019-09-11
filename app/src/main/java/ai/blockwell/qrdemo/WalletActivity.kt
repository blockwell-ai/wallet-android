package ai.blockwell.qrdemo

import android.content.ClipData
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ai.blockwell.qrdemo.api.Auth
import ai.blockwell.qrdemo.api.toDecimals
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.qr.TransactionQrActivity
import ai.blockwell.qrdemo.trainer.TrainerActivity
import ai.blockwell.qrdemo.view.TransferAdapter
import ai.blockwell.qrdemo.viewmodel.WalletModel
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnLayout
import com.google.android.material.snackbar.Snackbar
import com.takusemba.spotlight.OnSpotlightStateChangedListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.target.SimpleTarget
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

/**
 * The main wallet activity.
 */
class WalletActivity : AppCompatActivity() {

    val auth: Auth by inject()
    val scope = MainScope()
    val model by viewModel<WalletModel>()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null
    var spotlight = false

    lateinit var adapter: TransferAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        setSupportActionBar(toolbar)
        title = ""
        wallet_balance_text.text = getString(R.string.wallet_coin_balance, DataStore.tokenName)
        token_symbol.text = DataStore.tokenSymbol

        adapter = TransferAdapter()
        recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recycler.adapter = adapter

        val decorator = androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)

        fab.setOnClickListener {
            startActivity<SendActivity>()
        }
        account_address.setOnClickListener {
            val clipData = ClipData.newPlainText("Account address", account_address.text)
            clipboardManager.primaryClip = clipData
            longToast(R.string.account_copied)
        }

        if (!DataStore.introShown) {
            recycler.doOnLayout {
                val x = displayMetrics.widthPixels - 112f * displayMetrics.density
                val y = 52f * displayMetrics.density
                Log.d("Overlay", "$x - $y")
                val target = SimpleTarget.Builder(this)
                        .setPoint(x, y)
                        .setOverlayPoint(16f * displayMetrics.density, 100f * displayMetrics.density)
                        .setShape(Circle(30f * displayMetrics.density))
                        .setTitle("Scan QR Codes")
                        .setDescription("Easily perform transactions on the blockchain by scanning QR Codes.")
                        .build()

                Spotlight.with(this)
                        .setOverlayColor(R.color.overlay)
                        .setAnimation(DecelerateInterpolator(2f))
                        .setDuration(500)
                        .setTargets(target)
                        .setClosedOnTouchedOutside(true)
                        .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                            override fun onStarted() {
                                spotlight = true
                            }

                            override fun onEnded() {
                                spotlight = false
                            }
                        })
                        .start()
            }
            DataStore.introShown = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_wallet, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                toast(R.string.refreshing_balance)
                scope.launch { model.balance.refreshBalance() }
                true
            }
            R.id.action_qr -> {
                startActivity<TransactionQrActivity>()
                true
            }
            R.id.action_trainer -> {
                startActivity<TrainerActivity>()
                true
            }
            R.id.action_licenses -> {
                startActivity<LicensesActivity>()
                true
            }
            R.id.action_signout -> {
                auth.signOut()
                startActivity(intentFor<MainActivity>().clearTask().newTask())
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()

        if (!auth.isLoggedIn()) {
            startActivity(intentFor<MainActivity>().clearTask().newTask())
        } else {
            subscribeToUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
        job = null
    }

    override fun onBackPressed() {
        if (spotlight) {
            Spotlight.with(this).closeSpotlight()
            spotlight = false
        } else {
            super.onBackPressed()
        }
    }

    fun setBalance(newBalance: String): Boolean {
        val oldText = balance.text
        balance.text = newBalance.toDecimals(DataStore.tokenDecimals)
        return oldText.isNotEmpty() && oldText != balance.text
    }

    /**
     * Convenience method for subscribing this activity to background updates.
     */
    fun subscribeToUpdates() {
        val newJob = scope.launch {
            launch { subscribeToBalance() }
            launch { subscribeToTransfersHistory() }
            launch { subscribeToTransferStatus() }
        }
        job = newJob
    }

    /**
     * Subscribe to wallet balance updates.
     */
    suspend fun subscribeToBalance() {
        model.balance.channel.consumeEach {
            if (setBalance(it.balance)) {
                // Update transfers list if balance changed
                model.transfers.refresh()
            }
            account_address.text = it.account
            adapter.userAccount = it.account
        }
    }

    /**
     * Subscribe to transfers history data updates.
     */
    suspend fun subscribeToTransfersHistory() {
        model.transfers.channel.consumeEach {
            adapter.setTransfers(it)
        }
    }

    /**
     * Subscribe to status updates for a pending token transfer.
     */
    suspend fun subscribeToTransferStatus() {
        if (model.getPendingTransfer().isNotEmpty()) {
            val snackbar = Snackbar.make(main_layout, R.string.pending_transfer, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()

            model.transferStatus.channel.consumeEach {
                if (it.status == "completed") {
                    snackbar.dismiss()
                } else if (it.status == "error") {
                    alert(getString(R.string.transfer_failed) + it.error).show()
                    snackbar.dismiss()
                }
            }
        }
    }
}
