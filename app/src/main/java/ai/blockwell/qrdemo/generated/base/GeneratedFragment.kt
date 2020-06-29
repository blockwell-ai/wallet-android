package ai.blockwell.qrdemo.generated.base

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_trainer.*
import kotlinx.coroutines.MainScope
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * A placeholder fragment containing a simple view.
 */
class GeneratedFragment : Fragment() {

    val scope = MainScope()
    val model by sharedViewModel<TrainerModel>()
    lateinit var adapter: GeneratedOptionAdapter

    private var callback: OnOptionSelectedListener? = null

    fun setOptionSelectedListener(listener: OnOptionSelectedListener) {
        callback = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_generated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GeneratedOptionAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(requireContext(), R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)

        adapter.onClickListener = {
            callback?.onOptionSelected(it)
        }
    }

    interface OnOptionSelectedListener {
        fun onOptionSelected(option: Int)
    }
}
