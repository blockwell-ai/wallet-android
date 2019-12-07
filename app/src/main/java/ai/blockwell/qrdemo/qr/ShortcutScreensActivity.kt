package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.data.ShortcutConfig
import ai.blockwell.qrdemo.qr.view.ShortcutScreenAdapter
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_shortcut_screens.*
import org.jetbrains.anko.startActivity

class ShortcutScreensActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcut_screens)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Shortcuts"

        val screens = ShortcutConfig.config.screens
        val adapter = ShortcutScreenAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val decorator = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)

        adapter.setScreens(screens)

        adapter.onClickListener = {
            val index = screens.indexOf(it)

            startActivity<ShortcutsActivity>("screen" to index)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}