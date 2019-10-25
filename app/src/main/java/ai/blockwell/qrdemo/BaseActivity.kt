package ai.blockwell.qrdemo

import ai.blockwell.qrdemo.api.SystemStatus
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.maintenance.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity(), SystemStatus.StatusSubscriber {
    val scope = MainScope()
    val systemStatus: SystemStatus by inject()

    override fun onResume() {
        super.onResume()
        onStatusChanged(systemStatus.status, systemStatus.message)
        systemStatus.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        systemStatus.unsubscribe(this)
    }

    override fun onStatusChanged(status: String, message: String) {
        scope.launch {
            if (status == "maintenance") {
                maintenance_message.text = message
                maintenance.visibility = View.VISIBLE
            } else {
                maintenance.visibility = View.GONE
            }
        }
    }
}
