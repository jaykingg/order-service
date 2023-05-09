package io.hobbyful.orderservice.payment

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.time.Instant

class IsoDateDeserializer : StdDeserializer<Instant>(Instant::class.java) {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Instant =
        when (parser.valueAsLong) {
            0L -> Instant.parse(parser.text)
            else -> Instant.ofEpochMilli(parser.longValue)
        }
}
