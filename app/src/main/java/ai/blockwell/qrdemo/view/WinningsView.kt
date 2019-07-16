package ai.blockwell.qrdemo.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.LogEvent
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.api.toDecimals
import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.view_winnings.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.layoutInflater

@SuppressLint("ViewConstructor")
class WinningsView(context: Context, val client: ApiClient) : FrameLayout(context) {
    val scope = MainScope()
    val proxy = Proxy(client)

    init {
        context.layoutInflater.inflate(R.layout.view_winnings, this, true)
    }

    /*
    event ItemDropped(address indexed winner, address indexed token, uint256 dropRate, uint256 dropAmount);

    event TokenWin(address indexed winner, address token, uint256 value);
     */
    fun update(wins: List<LogEvent>) {
        scope.launch {
            wins.forEach {
                var symbol = ""
                var decimals = 0
                var address = it.returnValues["token"]!!
                var value = ""

                if (it.event == "ItemDropped") {
                    value = it.returnValues["dropAmount"] ?: "0"
                } else if (it.event == "TokenWin") {
                    value = it.returnValues["value"] ?: "0"
                }

                when (address.toLowerCase()) {
                    "0x618e75ac90b12c6049ba3b27f5d5f8651b0037f6" -> {
                        symbol = "QASH"
                        decimals = 6
                    }
                    "0x085935a183d1653846be497f9dddfa006b1aa6f0" -> {
                        symbol = "cat food"
                    }
                    "0x446d5f845da567a16cc58e7badee19a47e57d3b9" -> {
                        symbol = "litterbox"
                    }
                    "0x9e04f730827d570fc5bc2971a6327be8815a67de" -> {
                        symbol = "vet visit"
                    }
                    "0xe71e85438a770b00019108217f04b5738b7a495e" -> {
                        symbol = "cat toy"
                    }
                }

                val view = AppCompatTextView(context)

                view.text = "${value.toDecimals(decimals)} $symbol"

                winnings.addView(view)
            }
        }
    }
}
