package io.omonia.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static  <T> String toJsonString(T object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }
}
