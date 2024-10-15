package com.liu.yuojcodesandbox.MyCodeSandBox;

import com.liu.yuojcodesandbox.model.CodeSandboxCmd;

import java.io.File;

/**
 * @author 刘渠好
 * @since 2024-10-15 23:28
 */
public class MyJavaNativeCodeSandBox extends MyJavaNativeCodeSandBoxTemplate {
    //存放代码路径
    public static final  String CODE_TEMPLATE_PATH= File.pathSeparator+"tempCode";

    //目录前缀
    public static final  String PREFIX_NAME=File.pathSeparator+"java";

    //指定文件名
    public static final  String MAIN_NAME=File.pathSeparator+"Main.java";

    public MyJavaNativeCodeSandBox(){
        super.CODE_TEMPLATE_PATH=CODE_TEMPLATE_PATH;
        super.PREFIX_NAME=PREFIX_NAME;
        super.MAIN_NAME=MAIN_NAME;
    }

    @Override
    CodeSandboxCmd getCmd(String userCodePath, String userCodeParentPath) {
        return CodeSandboxCmd
                .builder ()
                .compileCmd (String.format ("javac -encoding utf-8 %s",userCodePath))
                .runCmd (String.format ("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main",userCodeParentPath))
                .build ();
    }
}
