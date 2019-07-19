package ai.blockwell.qrdemo.generated.base

import ai.blockwell.qrdemo.generated.funcList
import ai.blockwell.qrdemo.trainer.EnableMintingFragment
import ai.blockwell.qrdemo.trainer.TrainerOption
import ai.blockwell.qrdemo.trainer.TrainerOptionHolder
import ai.blockwell.qrdemo.trainer.freeze.FreezeFragment
import ai.blockwell.qrdemo.trainer.suggestions.SuggestionsFragment
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class GeneratedOptionAdapter : RecyclerView.Adapter<GeneratedOptionHolder>() {
    public var onClickListener: ((Int) -> Unit)? = null

    private var items = funcList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GeneratedOptionHolder(GeneratedOptionView(parent.context))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: GeneratedOptionHolder, position: Int) {
        val option = items[position]
        holder.bind(option)
        holder.view.setOnClickListener {
            onClickListener?.let {
                it(position)
            }
        }
    }
}

class GeneratedOptionHolder(val view: GeneratedOptionView) : RecyclerView.ViewHolder(view) {
    fun bind(option: String) {
        view.update(option)
    }
}
