package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.LogEvent
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.api.toDecimals
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.view_winnings.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.layoutInflater
import java.lang.Exception

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
    fun update(network: String, wins: List<LogEvent>) {
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

                try {
                    symbol = proxy.callAddress(network, address, "erc20", "symbol").get().data.asString
                } catch (e: Exception) {
                    Log.e("WinningsView", "Exception in getting symbol for winning", e)
                }

                try {
                    decimals = proxy.callAddress(network, address, "erc20", "decimals").get().data.asString.toInt()
                } catch (e: Exception) {
                    Log.e("WinningsView", "Exception in getting decimals for winning", e)
                }

                val view = AppCompatTextView(context)

                view.text = "${value.toDecimals(decimals)} $symbol"

                winnings.addView(view)
            }
        }
    }
}
