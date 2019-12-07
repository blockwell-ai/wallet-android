package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.data.ShortcutScreenConfig
import ai.blockwell.qrdemo.trainer.freeze.FreezeFragment
import ai.blockwell.qrdemo.trainer.suggestions.SuggestionsFragment
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
//IMPORT

class ShortcutScreenAdapter : RecyclerView.Adapter<ShortcutScreenHolder>() {
    public var onClickListener: ((ShortcutScreenConfig) -> Unit)? = null

    private var items = listOf<ShortcutScreenConfig>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutScreenHolder {
        val view = ShortcutScreenView(parent.context)
        view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        return ShortcutScreenHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ShortcutScreenHolder, position: Int) {
        val option = items[position]
        holder.bind(option)
        holder.view.setOnClickListener {
            onClickListener?.let {
                it(option)
            }
        }
    }

    fun setScreens(screens: List<ShortcutScreenConfig>) {
        items = screens
        notifyDataSetChanged()
    }
}

class ShortcutScreenHolder(val view: ShortcutScreenView) : RecyclerView.ViewHolder(view) {
    fun bind(option: ShortcutScreenConfig) {
        view.update(option)
    }
}
