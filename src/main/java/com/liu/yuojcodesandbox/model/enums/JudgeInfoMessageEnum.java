package com.liu.yuojcodesandbox.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author 刘渠好
 * @since 2024/8/11 下午10:10
 * 判题信息消息枚举
 */
@Getter
public enum JudgeInfoMessageEnum {

    /**
     * 判题消息
     */

    ACCEPTED("运行成功", "Accepted"),
    WRONG_ANSWER("答案错误", "Wrong Answer"),
    COMPILE_ERROR("编译错误", "Compile Error"),
    MEMORY_LIMIT_EXCEEDED("内存溢出", "Memory Limit Exceeded"),
    TIME_LIMIT_EXCEEDED("超时", "Time Limit Exceeded"),
    PRESENTATION_ERROR("展示错误", "Presentation Error"),
    WAITING("等待中", "Waiting"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出", "Output Limit Exceeded"),
    DANGEROUS_OPERATION("危险操作", "Dangerous Operation"),
    RUNTIME_ERROR("运行错误", "Runtime Error"),
    SYSTEM_ERROR("系统错误", "System Error");


    private final String text;
    private final String value;

    private JudgeInfoMessageEnum(String text,String value){
        this.text=text;
        this.value=value;
    }

    //获取值列表
    public List<String> getValues(){
        JudgeInfoMessageEnum[] values = values();
        return Arrays.stream(values).map(item->getValue()).collect(Collectors.toList());
    }

    //根据值找枚举类
    public JudgeInfoMessageEnum getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (JudgeInfoMessageEnum infoMessageEnum : values()) {
            if (infoMessageEnum.value.equals(value)){
                return infoMessageEnum;
            }
        }
        return null;
    }
}
