package com.liu.yuojcodesandbox.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 刘渠好
 * @since 2024/8/12 下午10:33
 * 题目提交语言枚举类
 */
@Getter
public enum QuestionSubmitLanguageEnum {
    JAVA("java","java"),
    PYTHON("python","python");

     private final String text;

     private final String value;

     QuestionSubmitLanguageEnum(String text,String value){
        this.text=text;
        this.value=value;
    }

    //查找所有枚举值
    public static List<String> getValues(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }

    //根据value找枚举值
    public static QuestionSubmitLanguageEnum getLanguageEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (QuestionSubmitLanguageEnum questionSubmitLanguageEnum : values()) {
            if (questionSubmitLanguageEnum.getValue().equals(value)){
                return questionSubmitLanguageEnum;
            }
        }
        return null;
    }
}
