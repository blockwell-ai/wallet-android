package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.trainer.FragmentProvider
import ai.blockwell.qrdemo.trainer.GuidedStepsFragment

class SuggestionsFragment : GuidedStepsFragment() {

    override fun fragmentsList() = listOf<FragmentProvider>(
            { Step1Fragment() },
            { Step2Fragment() },
            { Step3Fragment() },
            { Step4Fragment() },
            { Step5Fragment() }
    )

    override fun onResume() {
        super.onResume()

        requireActivity().title = "Suggestions and Voting"
    }
}