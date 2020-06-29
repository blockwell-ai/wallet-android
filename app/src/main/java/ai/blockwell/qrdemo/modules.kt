package ai.blockwell.qrdemo

import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.utils.ArgumentValueTypeAdapter
import ai.blockwell.qrdemo.utils.TransactionErrorTypeAdapter
import ai.blockwell.qrdemo.utils.argumentValueDeserializer
import ai.blockwell.qrdemo.utils.argumentValueSerializer
import ai.blockwell.qrdemo.viewmodel.*
import coil.Coil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module.
 */
val mainModule = module {
    single {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(ArgumentValue::class.java, ArgumentValueTypeAdapter())
        builder.registerTypeAdapter(TransactionError::class.java, TransactionErrorTypeAdapter())

        builder.create()
    }
    single { ApiClient(get()) }
    single { Auth(get()) }
    single { Tx(get()) }
    single { Tokens(get()) }
    single { Proxy(get()) }
    single { SystemStatus(get()) }
    single { Coil.imageLoader(androidApplication())}

    viewModel { WalletModel(get()) }
    viewModel { SendModel(get()) }
    viewModel { TxModel(get(), get()) }
    viewModel { TrainerModel(get(), get()) }
    viewModel { VotingModel(get(), get()) }
}
