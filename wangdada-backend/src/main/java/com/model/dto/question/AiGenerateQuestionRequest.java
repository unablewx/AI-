package com.model.dto.question;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiGenerateQuestionRequest implements Serializable {
    /**
     * 应用id
     */
    private String appId;
    /**
     * 生成题目数量
     */
    private int questionNumber;
    /**
     * 选项数
     */
    private int optionNumber;
    private final static long serialVersionUID = 1L;
}
