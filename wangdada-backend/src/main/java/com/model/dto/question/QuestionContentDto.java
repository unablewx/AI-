package com.model.dto.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionContentDto {
    /**
     * 题目标题
     */
    private String title;

    /**
     * 选项
     */
    private List<Option> options;

    @Data
    public static class Option {
        /**
         * 选项结果
         */
        private String result;

        /**
         * 选项值
         */
        private String value;

        /**
         * 选项键
         */
        private String key;
        /**
         * 分值
         */
        private int score;
    }
}
