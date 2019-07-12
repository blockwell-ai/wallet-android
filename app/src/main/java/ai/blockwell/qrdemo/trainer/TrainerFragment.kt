package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Etherscan
import ai.blockwell.qrdemo.api.toDecimals
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kittinunf.result.success
import kotlinx.android.synthetic.main.fragment_trainer.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.android.architecture.ext.sharedViewModel

/**
 * A placeholder fragment containing a simple view.
 */
class TrainerFragment : Fragment() {

    val scope = MainScope()
    val model by sharedViewModel<TrainerModel>()
    var job: Job? = null
    lateinit var adapter: TrainerOptionAdapter

    private var callback: OnOptionSelectedListener? = null

    fun setOptionSelectedListener(listener: OnOptionSelectedListener) {
        callback = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trainer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrainerOptionAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(requireContext(), R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)

        adapter.onClickListener = {
            callback?.onOptionSelected(it)
        }
    }

    override fun onResume() {
        super.onResume()
        subscribe()
        requireActivity().title = "Token Trainer"
    }

    override fun onPause() {
        job?.cancel()
        job = null
        super.onPause()
    }

    private fun subscribe() {
        job = scope.launch {
            model.channel.channel.consumeEach {
                val address = it.address
                if (address != null) {
                    updateBalance()
                    trainer_address.text = address
                    trainer_token.setOnClickListener {
                        val webpage = Uri.parse(Etherscan.token("rinkeby", address))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(requireActivity().packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                } else {
                    trainer_balance.setText(R.string.pending_trainer)
                    trainer_address.setText(R.string.pending_trainer)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBalance() {
        scope.launch {
            val result = model.getBalance()
            result.success {
                trainer_balance.text = it.data.asString.toDecimals(18) + " TRAIN"
            }
        }
    }

    interface OnOptionSelectedListener {
        fun onOptionSelected(option: TrainerOption)
    }
}
