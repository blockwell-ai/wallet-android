package ai.blockwell.qrdemo

import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.utils.ArgumentValueTypeAdapter
import ai.blockwell.qrdemo.utils.argumentValueDeserializer
import ai.blockwell.qrdemo.utils.argumentValueSerializer
import ai.blockwell.qrdemo.viewmodel.SendModel
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import ai.blockwell.qrdemo.viewmodel.TxModel
import ai.blockwell.qrdemo.viewmodel.WalletModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext

/**
 * Koin dependency injection module.
 */
val mainModule = applicationContext {
    bean {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(ArgumentValue::class.java, ArgumentValueTypeAdapter())

        builder.create()
    }
    bean { ApiClient(get()) }
    bean { Auth(get()) }
    bean { Tx(get()) }
    bean { Tokens(get()) }
    bean { Proxy(get()) }
    viewModel { WalletModel(get()) }
    viewModel { SendModel(get()) }
    viewModel { TxModel(get()) }
    viewModel { TrainerModel(get(), get()) }
}
