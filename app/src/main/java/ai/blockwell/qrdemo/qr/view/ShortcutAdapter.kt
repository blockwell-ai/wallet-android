package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.api.TxResponse
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShortcutAdapter : RecyclerView.Adapter<ShortcutHolder>() {
    public var onClickListener: ((TxResponse) -> Unit)? = null

    private var items = listOf<TxResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutHolder {
        val view = ShortcutView(parent.context)
        view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        return ShortcutHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ShortcutHolder, position: Int) {
        val option = items[position]
        holder.bind(option)
        holder.view.setOnClickListener {
            onClickListener?.let {
                it(option)
            }
        }
    }

    fun setCodes(screens: List<TxResponse>) {
        items = screens
        notifyDataSetChanged()
    }
}

class ShortcutHolder(val view: ShortcutView) : RecyclerView.ViewHolder(view) {
    fun bind(option: TxResponse) {
        view.update(option)
    }
}
