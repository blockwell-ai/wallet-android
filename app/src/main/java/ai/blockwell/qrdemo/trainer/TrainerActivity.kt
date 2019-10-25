package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.generated.base.GeneratedActivity
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_trainer.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.koin.android.architecture.ext.viewModel

class TrainerActivity : BaseActivity(), TrainerFragment.OnOptionSelectedListener {

    val model by viewModel<TrainerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer)
        setSupportActionBar(toolbar)
        title = getString(R.string.token_trainer)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment, TrainerFragment())
        }.commit()

        loadToken()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_trainer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_generated -> {
                startActivity<GeneratedActivity>()
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()

        if (!DataStore.trainerIntroShown) {
            alert(R.string.trainer_intro).show()

            DataStore.trainerIntroShown = true
        }
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if (fragment is TrainerFragment) {
            Log.d("TrainerActivity", "Added listener")
            fragment.setOptionSelectedListener(this)
        }
    }

    private fun loadToken() {
        val snackbar = Snackbar.make(main_layout, R.string.pending_trainer, Snackbar.LENGTH_INDEFINITE)

        scope.launch {
            model.channel.channel.consumeEach {
                if (it.address == null) {
                    snackbar.show()
                } else {
                    if (snackbar.isShown) {
                        snackbar.dismiss()
                    }
                }
            }
        }
    }

    override fun onOptionSelected(option: TrainerOption) {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment, option.factory())
                .commit()
    }
}
