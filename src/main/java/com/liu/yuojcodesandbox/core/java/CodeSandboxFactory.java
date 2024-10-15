package com.liu.yuojcodesandbox.core.java;

import com.liu.yuojcodesandbox.model.enums.QuestionSubmitLanguageEnum;


/**
 * @author 刘渠好
 * @since 2024/8/12 下午10:31
 * 沙箱工厂模式
 *
 */
public class CodeSandboxFactory {

    public static CodeSandboxTemplate getInstance(QuestionSubmitLanguageEnum language){
        /**
         * 版本升级新写法(JDK14+才可以这么使用)
         * return switch (language){
                       case JAVA -> new JavaNativeCodeSandbox();
                       case PYTHON -> throw new RuntimeException("正在更新");
                       default -> throw new RuntimeException("暂不支持");
                   };
         */
        CodeSandboxTemplate codeSandboxTemplate;
        switch (language){
            case JAVA:
                codeSandboxTemplate=new JavaNativeCodeSandbox();
                break;
            case PYTHON:
                throw new RuntimeException("正在更新，请持续关注!");
            default:
                throw new RuntimeException("暂不支持!");
        }
        return codeSandboxTemplate;

    }
}
