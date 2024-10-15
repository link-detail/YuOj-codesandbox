package com.liu.yuojcodesandbox.core.java;

import com.liu.yuojcodesandbox.model.CodeSandboxCmd;

import java.io.File;

/**
 * @author 刘渠好
 * @since 2024/8/11 下午9:29
 * java原生代码沙箱
 */
public class JavaNativeCodeSandbox extends CodeSandboxTemplate {
    String prefix= File.separator+"java";

    String globalCodePath=File.separator+"tempCode";

    String globalCodeFileName=File.separator+"Main.java";

    public JavaNativeCodeSandbox(){
        super.prefix=prefix;
        super.globalCodePath=globalCodePath;
        super.globalCodeFileName=globalCodeFileName;
    }


    //获取执行命令
    @Override
    public CodeSandboxCmd getCmd(String userCodePath, String userCodeParentPath) {
        return CodeSandboxCmd.builder()
                //编译代码 javac -encoding utf-8 xxx.java
                .compileCmd(String.format("javac -encoding utf-8 %s",userCodePath))
                //运行代码 java -Dfile.encoding=utf-8 -cp xxx Main
                .runCmd(String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main",userCodeParentPath))
                .build();
    }
}
