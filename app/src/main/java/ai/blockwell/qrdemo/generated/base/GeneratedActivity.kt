package ai.blockwell.qrdemo.generated.base

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.generated.AAAGeneratedPagingFragment
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_trainer.*
import kotlinx.coroutines.MainScope
import org.koin.android.architecture.ext.viewModel

class GeneratedActivity : AppCompatActivity(), GeneratedFragment.OnOptionSelectedListener {

    val scope = MainScope()
    val model by viewModel<TrainerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated)
        setSupportActionBar(toolbar)
        title = getString(R.string.generated_fragments)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment, GeneratedFragment())
        }.commit()
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if (fragment is GeneratedFragment) {
            Log.d("TrainerActivity", "Added listener")
            fragment.setOptionSelectedListener(this)
        }
    }

    override fun onOptionSelected(option: Int) {
        val frag = AAAGeneratedPagingFragment()
        frag.position = option

        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment, frag)
                .commit()
    }
}
