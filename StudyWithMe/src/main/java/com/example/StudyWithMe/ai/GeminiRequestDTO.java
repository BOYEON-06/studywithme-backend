package com.example.StudyWithMe.ai;

import java.util.List;

// 2. Gemini API 요청용 (구조가 다소 복잡함)
public record GeminiRequestDTO(List<Content> contents) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
