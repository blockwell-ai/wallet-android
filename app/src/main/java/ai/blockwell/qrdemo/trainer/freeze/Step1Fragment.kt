package ai.blockwell.qrdemo.trainer.freeze

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_freeze_step1.*

class Step1Fragment : StepFragment() {
    override val layoutRes = R.layout.fragment_freeze_step1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons.hideBack()
    }
}
