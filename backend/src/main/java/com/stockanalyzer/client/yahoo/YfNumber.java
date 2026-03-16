package com.stockanalyzer.client.yahoo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.IOException;

/**
 * Represents a Yahoo Finance numeric field which may be either:
 *   - a plain JSON number: {@code 28.5}
 *   - a {@code {"raw": 28.5, "fmt": "28.50"}} object
 *
 * Use {@link #asDouble()} or {@link #asLong()} to extract the value.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@JsonDeserialize(using = YfNumber.Deserializer.class)
public final class YfNumber {

    public static final YfNumber ZERO = new YfNumber(0.0);

    double value;

    public YfNumber(double value) {
        this.value = value;
    }

    public double asDouble() {
        return value;
    }

    public long asLong() {
        return (long) value;
    }

    static final class Deserializer extends JsonDeserializer<YfNumber> {
        @Override
        public YfNumber deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node == null || node.isNull() || node.isMissingNode()) return ZERO;
            if (node.isNumber()) return new YfNumber(node.asDouble());
            if (node.isObject()) {
                JsonNode raw = node.get("raw");
                if (raw != null && raw.isNumber()) return new YfNumber(raw.asDouble());
            }
            return ZERO;
        }
    }
}
