package ai.blockwell.qrdemo.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Transfer
import ai.blockwell.qrdemo.api.toDecimals
import ai.blockwell.qrdemo.data.DataStore
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_transfer.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource

class TransferAdapter : RecyclerView.Adapter<TransferHolder>() {
    private var items = listOf<Transfer>()
    var userAccount: String = ""

    fun setTransfers(transfers: Array<Transfer>) {
        items = transfers.sortedByDescending { it.blockNumber }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = TransferHolder(parent.context.layoutInflater.inflate(R.layout.item_transfer, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TransferHolder, position: Int) {
        val transfer = items[position]
        holder.bind(transfer, transfer.to.compareTo(userAccount, true) == 0)
    }
}

class TransferHolder(val view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("SetTextI18n")
    fun bind(transfer: Transfer, received: Boolean) {
        val res = view.resources
        val value = transfer.value.toDecimals(DataStore.tokenDecimals)
        if (received) {
            view.direction.setText(R.string.received)
            view.direction.textColorResource = R.color.colorTextEmphasis

            view.value.text = value + " " + DataStore.tokenSymbol
            view.value.textColorResource = R.color.colorTextEmphasis

            view.address.text = res.getString(R.string.transfer_address,
                    res.getString(R.string.from),
                    transfer.from)
        } else {
            view.direction.setText(R.string.sent)
            view.direction.textColorResource = R.color.error

            view.value.text = "-" + value + " " + DataStore.tokenSymbol
            view.value.textColorResource = R.color.error

            view.address.text = res.getString(R.string.transfer_address,
                    res.getString(R.string.to),
                    transfer.to)
        }

        view.blocknum.text = res.getString(R.string.block_number, transfer.blockNumber)
    }
}
