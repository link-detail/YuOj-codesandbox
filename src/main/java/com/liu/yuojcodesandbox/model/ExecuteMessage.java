package com.liu.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进程执行信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteMessage {

    //信息码
    private Integer exitValue;

    //正常信息
    private String message;

    //错误信息
    private String errorMessage;

    //执行时间
    private Long time;

    //执行内存
    private Long memory;
}
