package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.data.DataStore
import android.util.Log
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.text.DecimalFormat

val decimalFormat = DecimalFormat("#,###.######")

fun String.toDecimals(decimals: Int): String {
    val dec = BigDecimal(this)
            .scaleByPowerOfTen(-1 * decimals)

    return decimalFormat.format(dec)
}

fun String.fromDecimals(decimals: Int): String {
    var result = ""

    try {
        val dec = BigDecimal(this)
                .scaleByPowerOfTen(decimals)
        result = dec.toPlainString()
    } catch (e: NumberFormatException) {
        Log.d("Utils", "Failed to format input: $this")
    }

    return result
}
