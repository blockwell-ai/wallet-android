package ai.blockwell.qrdemo.trainer.freeze

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import ai.blockwell.qrdemo.utils.boolean
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_freeze_step3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Step3Fragment : StepFragment(), Events.Subscriber {
    override val layoutRes = R.layout.fragment_freeze_step3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons.hideBack()
        load()
    }

    override fun onEvent(type: Events.Type, data: Any) {
        if (type == Events.Type.REFRESH) {
            load()
        }
    }

    fun load() {
        scope.launch {
            delay(1000)
            if (isAdded) {
                val result = model.call("isFrozen", listOf(user_wallet.text.toString()))

                try {
                    frozen_status.boolean(result.get().data.asBoolean)
                } catch (e: Exception) {
                    Log.e("Step3Fragment", "Error getting frozen status", e)
                }
            }
        }
    }
}
