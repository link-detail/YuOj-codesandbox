package com.liu.yuojcodesandbox.model.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author 刘渠好
 * @since 2024-08-28 22:36
 * 镜像枚举类
 */
@Getter
@AllArgsConstructor
public enum LanguageImageEnum {

    JAVA8("java8","openjdk:8-alpine"),
    JAVA11("java11","openjdk:11")
    ;

    private final String language;

    private final String image;

    //获取所有镜像列表
    public List<String> getImages(){
        return Arrays.stream(values ()).map (LanguageImageEnum::getImage).collect(Collectors.toList());

    }
}
