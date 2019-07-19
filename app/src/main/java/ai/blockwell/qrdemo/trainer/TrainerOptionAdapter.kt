package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.trainer.freeze.FreezeFragment
import ai.blockwell.qrdemo.trainer.suggestions.SuggestionsFragment
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
//IMPORT

class TrainerOptionAdapter : RecyclerView.Adapter<TrainerOptionHolder>() {
    public var onClickListener: ((TrainerOption) -> Unit)? = null

    private var items = listOf(
            TrainerOption("Guided 1: Enable Minting",
                    "Enable token minting on your Trainer Token and mint more tokens.") { EnableMintingFragment() },
            TrainerOption("Guided 2: Suggestions and Voting",
                    "Add Suggestions users can vote on.") { SuggestionsFragment() },
            TrainerOption("Guided 3: Freezing Wallets",
                    "Freeze wallets to prevent their use.") { FreezeFragment() }
//FLOW
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrainerOptionHolder(TrainerOptionView(parent.context))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TrainerOptionHolder, position: Int) {
        val option = items[position]
        holder.bind(option)
        holder.view.setOnClickListener {
            Log.d("TrainerAdapter", "Adapter click ${option.title}")
            onClickListener?.let {
                it(option)
            }
        }
    }
}

data class TrainerOption(
        val title: String,
        val description: String,
        val factory: () -> Fragment
)

class TrainerOptionHolder(val view: TrainerOptionView) : RecyclerView.ViewHolder(view) {
    fun bind(option: TrainerOption) {
        view.update(option)
    }
}
