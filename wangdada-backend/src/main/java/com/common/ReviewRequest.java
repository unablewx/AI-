package com.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 审核状态
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;
    /**
     * 审核人
     */
//    private int userId;
    /**
     * 审核时间
     */
//    private Date reviewTime;
}
