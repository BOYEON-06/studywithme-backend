package com.example.StudyWithMe.ai;

public record AiParameterDTO(
        String topic,           // 주제 (예: 자바 인터페이스)
        String difficulty,      // 난이도 (예: 상, 중, 하)
        String additionalRequest // 추가 요청 (예: 코드가 포함되게 해줘, 실생활 예시로 들어줘)
) {}
