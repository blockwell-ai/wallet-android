package ai.blockwell.qrdemo.trainer

abstract class StepFragment : GuidedFragment() {

    fun next() {
        model.events.publish(Events.Type.NEXT)
    }
}