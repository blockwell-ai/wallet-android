package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.BuildConfig
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.data.ShortcutConfig
import ai.blockwell.qrdemo.qr.view.ShortcutAdapter
import ai.blockwell.qrdemo.qr.view.ShortcutScreenAdapter
import ai.blockwell.qrdemo.viewmodel.TxModel
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_shortcut_screens.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.koin.android.architecture.ext.viewModel

class ShortcutsActivity : BaseActivity() {
    val model by viewModel<TxModel>()
    lateinit var adapter: ShortcutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcut_screens)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ShortcutAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val decorator = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)

        adapter.onClickListener = {
            startActivity<TxActivity>("url" to "${BuildConfig.API_BASEURL}/${it.shortcode}")
        }

        val screen = ShortcutConfig.config.screens[intent.getIntExtra("screen", 0)]
        title = screen.title

        load(screen.shortcodes)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }

    fun load(codes: List<String>) {
        scope.launch {
            val result = model.getCodes(codes)

            result.fold({
                adapter.setCodes(it.data)
            }, {
                val message = it.message
                when {
                    systemStatus.error.isNotEmpty() -> alert(systemStatus.error).show()
                    message != null -> alert(message).show()
                    else -> alert(R.string.unknown_error).show()
                }
            })
        }
    }
}