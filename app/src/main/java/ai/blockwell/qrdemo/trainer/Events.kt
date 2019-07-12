package ai.blockwell.qrdemo.trainer

import android.util.Log

class Events {

    private var subscribers = listOf<Subscriber>()

    fun subscribe(sub: Subscriber) {
        subscribers = subscribers + sub
        Log.d("Events", "Added to subscribers:\n " + subscribers.joinToString("\n") { it::class.toString() })
    }

    fun unsubscribe(sub: Subscriber) {
        subscribers = subscribers - sub
        Log.d("Events", "Removed subscribers:\n " + subscribers.joinToString("\n") { it::class.toString() })
    }

    fun publish(type: Type) {

        Log.d("Events", "Firing event $type\n " + subscribers.joinToString("\n") { it::class.toString() })
        subscribers.forEach { it.onEvent(type, Any()) }
    }

    fun publish(type: Type, data: Any) {
        subscribers.forEach { it.onEvent(type, data) }
    }

    enum class Type {
        NEXT, FINISHED, BACK, REFRESH
    }

    interface Subscriber {
        fun onEvent(type: Type, data: Any)
    }
}