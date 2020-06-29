package ai.blockwell.qrdemo

import ai.blockwell.qrdemo.api.Auth
import ai.blockwell.qrdemo.api.Etherscan
import ai.blockwell.qrdemo.api.toDecimals
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.data.ShortcutConfig
import ai.blockwell.qrdemo.qr.ShortcutScreensActivity
import ai.blockwell.qrdemo.qr.TransactionQrActivity
import ai.blockwell.qrdemo.suggestions.SuggestionsActivity
import ai.blockwell.qrdemo.trainer.TrainerActivity
import ai.blockwell.qrdemo.view.TransferAdapter
import ai.blockwell.qrdemo.viewmodel.WalletModel
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.doOnLayout
import com.github.ajalt.timberkt.Timber
import com.google.android.material.snackbar.Snackbar
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * The main wallet activity.
 */
class WalletActivity : BaseActivity() {

    val auth: Auth by inject()
    val model : WalletModel by viewModel()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null
    var spotlight: Spotlight? = null

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
            startActivity<TransactionQrActivity>()
        }

        if (ShortcutConfig.config.displayFab) {
            shortcuts_fab.show()
            shortcuts_fab.setOnClickListener {
                startActivity<ShortcutScreensActivity>()
            }
        }

        account_address.setOnClickListener {
            val clipData = ClipData.newPlainText("Account address", account_address.text)
            clipboardManager.setPrimaryClip(clipData)
            longToast(R.string.account_copied)
        }
        wallet_menu.setOnClickListener {
            val popup = PopupMenu(this, wallet_menu)
            popup.menuInflater.inflate(R.menu.menu_wallet_address, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_qr -> {
                        startActivity<QrActivity>()
                        true
                    }
                    R.id.action_etherscan_mainnet -> {
                        val webpage = Uri.parse(Etherscan.wallet("main", DataStore.accountAddress))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                        true
                    }
                    R.id.action_etherscan_rinkeby -> {
                        val webpage = Uri.parse(Etherscan.wallet("rinkeby", DataStore.accountAddress))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        if (!DataStore.introShown) {
            recycler.doOnLayout {
                var x = displayMetrics.widthPixels - 45 * displayMetrics.density
                var y = displayMetrics.heightPixels - 45 * displayMetrics.density
                Timber.d { "$x - $y" }

                val targets = arrayListOf<Target>()

                val targetOne = layoutInflater.inflate(R.layout.spotlight_qr, FrameLayout(this))
                targetOne.setOnClickListener { spotlight?.next() }
                targets.add(Target.Builder()
                        .setAnchor(x, y)
                        .setOverlay(targetOne)
                        .setShape(Circle(45f * displayMetrics.density))
                        .build())

                x  = displayMetrics.widthPixels - 22 * displayMetrics.density
                y = 50f * displayMetrics.density

                val targetTwo = layoutInflater.inflate(R.layout.spotlight_suggestions, FrameLayout(this))
                targetTwo.setOnClickListener { spotlight?.next() }
                if (DataStore.suggestionsToken.isNotEmpty()) {
                    targets.add(Target.Builder()
                            .setAnchor(x, y)
                            .setOverlay(targetTwo)
                            .setShape(Circle(30f * displayMetrics.density))
                            .build())
                }

                spotlight = Spotlight.Builder(this)
                        .setBackgroundColor(R.color.overlay)
                        .setAnimation(DecelerateInterpolator(2f))
                        .setDuration(200)
                        .setTargets(targets)
                        .setOnSpotlightListener(object : OnSpotlightListener {
                            override fun onStarted() {
                            }

                            override fun onEnded() {
                                spotlight = null
                            }
                        })
                        .build()

                spotlight?.start()
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
            R.id.action_send -> {
                startActivity<SendActivity>()
                true
            }
            R.id.action_trainer -> {
                startActivity<TrainerActivity>()
                true
            }
            R.id.action_suggestions -> {
                startActivity<SuggestionsActivity>()
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
        val spot = spotlight
        if (spot != null) {
            spot.finish()
            spotlight = null
        } else {
            super.onBackPressed()
        }
    }

    fun setBalance(newBalance: String): Boolean {
        val oldText = balance.text
        balance.text = newBalance.toDecimals(DataStore.tokenDecimals)
        return oldText.isNotEmpty() && oldText.toString() != balance.text.toString()
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
