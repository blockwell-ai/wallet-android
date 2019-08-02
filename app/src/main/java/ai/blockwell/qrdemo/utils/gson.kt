package ai.blockwell.qrdemo.utils

import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.ArrayArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
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
    override fun write(out: JsonWriter, value: ArgumentValue) {
        if (value.isArray()) {
            out.beginArray()
            value.getArray().forEach { out.value(it) }
            out.endArray()
        } else {
            out.value(value.getValue())
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