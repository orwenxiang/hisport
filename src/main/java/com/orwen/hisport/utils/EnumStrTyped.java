package com.orwen.hisport.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public interface EnumStrTyped<T extends EnumStrTyped<T>> {
    String getType();

    class Deserializer<T extends EnumStrTyped<T>> extends JsonDeserializer<T> {
        private T[] types;

        @SneakyThrows
        protected T[] types() {
            if (types != null && types.length > 0) {
                return types;
            }
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof Class) {
                throw new IllegalArgumentException("Internal error:" +
                        " TypeReference constructed without actual type information");
            }

            Method method = ((Class) ((ParameterizedType) superClass).getActualTypeArguments()[0]).getMethod("values");

            method.setAccessible(true);

            types = (T[]) method.invoke(null);

            return types;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            for (T type : types()) {
                if (Objects.equals(type.getType(), value)) {
                    return type;
                }
            }
            return null;
        }
    }

    class Serializer extends JsonSerializer<EnumStrTyped> {
        @Override
        public void serialize(
                EnumStrTyped value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getType());
        }
    }

}
