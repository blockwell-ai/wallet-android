package ai.blockwell.qrdemo

import android.app.Application
import android.os.Build
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.gsonpref.gson
import com.facebook.stetho.Stetho
import com.github.ajalt.timberkt.Timber
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.stetho.StethoHook
import com.google.gson.Gson
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber.DebugTree

@Suppress("unused")
class WalletApplication : Application() {
    companion object {
        lateinit var app: Application
    }

    override fun onCreate() {
        super.onCreate()

        app = this

        Stetho.initializeWithDefaults(this)
        Kotpref.init(this)

        // Fuel defaults
        FuelManager.instance.basePath = BuildConfig.API_BASEURL
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "User-Agent" to "Blockwell-QR bwqrandroid ${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} " +
                        "(${Build.MANUFACTURER} ${Build.MODEL}) " +
                        "(Android SDK ${Build.VERSION.SDK_INT})"
        )
        FuelManager.instance.hook = StethoHook()

        startKoin {
            androidLogger()
            androidContext(this@WalletApplication)
            modules(mainModule)
        }

        // Need to inject this directly to Kotpref
        Kotpref.gson = get<Gson>()

        Timber.plant(DebugTree())
    }
}
