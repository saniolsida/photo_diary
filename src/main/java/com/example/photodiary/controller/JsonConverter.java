package com.example.photodiary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;

public class JsonConverter {
    // 공용으로 사용할 ObjectMapper 객체 (static)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 객체(List 등)를 JSON 문자열로 변환 (직렬화)
     */
    public static String toJson(Object object) {
        try {
            if (object == null) return "[]";
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            System.err.println("JSON 직렬화 에러: " + e.getMessage());
            return "[]";
        }
    }

    /**
     * JSON 문자열을 List<Long>으로 변환 (역직렬화)
     */
    public static List<Long> toLongList(String json) {
        try {
            if (json == null || json.isEmpty()) return new ArrayList<>();
            // TypeReference를 사용하여 List<Long> 타입임을 명시
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            System.err.println("JSON 역직렬화 에러: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}