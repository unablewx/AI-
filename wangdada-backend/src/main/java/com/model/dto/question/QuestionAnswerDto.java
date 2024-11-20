package com.model.dto.question;

import lombok.Data;

/**
 * 题目答案封装类(用于AI评分)
 */
@Data
public class QuestionAnswerDto {
    /**
     * 题目title
     */
    private String title;
    /**
     * 用户选择的答案
     */
    private String userAnswer;
}
