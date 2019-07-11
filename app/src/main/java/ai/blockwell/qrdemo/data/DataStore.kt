package ai.blockwell.qrdemo.data

import ai.blockwell.qrdemo.api.Transfer
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonPref

/**
 * Very simple data store using android SharedPreferences.
 */
object DataStore : KotprefModel() {
    var accessToken by stringPref()
    var tokenExpiration by longPref()
    var email by stringPref()
    var accountAddress by stringPref()
    var balance by stringPref()
    var transfers by gsonPref(arrayOf<Transfer>())
    var pendingTransfer by stringPref()
    var introShown by booleanPref(false)

    var trainerIntroShown by booleanPref(false)
    var trainerToken by stringPref("")
    var trainerTokenAddress by stringPref("")

    // Details on the token
    var tokenName by stringPref("")
    var tokenSymbol by stringPref("")
    var tokenDecimals by intPref(18)
}
