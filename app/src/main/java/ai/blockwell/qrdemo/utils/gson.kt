package ai.blockwell.qrdemo.utils

import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.ArrayArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
import ai.blockwell.qrdemo.api.TransactionError
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

val argumentValueDeserializer = JsonDeserializer<ArgumentValue> { json, _, _ ->
    when {
        json.isJsonNull -> null
        json.isJsonArray -> ArrayArgumentValue(json.asJsonArray.map { it.asString })
        else -> StringArgumentValue(json.asString)
    }
}

val argumentValueSerializer = JsonSerializer<ArgumentValue> { src, _, _ ->
    if (src.isArray()) {
        val arr = JsonArray()
        src.getArray().forEach {
            arr.add(it)
        }
        arr
    } else {
        JsonPrimitive(src.getValue())
    }
}

class ArgumentValueTypeAdapter : TypeAdapter<ArgumentValue>() {
    override fun write(out: JsonWriter, value: ArgumentValue?) {
        if (value != null) {
            if (value.isArray()) {
                out.beginArray()
                value.getArray().forEach { out.value(it) }
                out.endArray()
            } else {
                out.value(value.getValue())
            }
        } else {
            out.nullValue()
        }
    }

    override fun read(reader: JsonReader): ArgumentValue? {
        val token = reader.peek()

        if (token == JsonToken.BEGIN_ARRAY) {
            val array = mutableListOf<String>()
            reader.beginArray()
            while (reader.hasNext()) {
                array.add(reader.nextString())
            }
            reader.endArray()
            return ArrayArgumentValue(array)
        }

        if (token == JsonToken.STRING) {
            return StringArgumentValue(reader.nextString())
        }

        return null
    }
}

class TransactionErrorTypeAdapter : TypeAdapter<TransactionError>() {
    override fun write(out: JsonWriter, value: TransactionError?) {
        if (value != null) {
            out.beginObject()
            out.name("code")
            out.value(value.code)
            out.name("message")
            out.value(value.message)

            if (value.gasRequired != null) {
                out.name("gasRequired")
                out.value(value.gasRequired)
            }
            out.endObject()
        } else {
            out.nullValue()
        }
    }

    override fun read(reader: JsonReader): TransactionError? {
        val token = reader.peek()

        if (token == JsonToken.BEGIN_OBJECT) {
            reader.beginObject()
            var code: String? = null
            var message: String? = null
            var gasRequired: String? = null

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "code" ->  {
                         code = reader.nextString()
                    }
                    "message" -> {
                        message = reader.nextString()
                    }
                    "gasRequired" -> {
                        gasRequired = reader.nextString()
                    }
                }
            }
            reader.endObject()

            if (code != null && message != null) {
                return TransactionError(code, message, gasRequired)
            }
        }

        if (token == JsonToken.STRING) {
            return TransactionError("old_error", reader.nextString())
        }

        return null
    }
}