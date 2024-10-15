package com.liu.yuojcodesandbox.codesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.liu.yuojcodesandbox.core.CodeSandbox;
import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;
import com.liu.yuojcodesandbox.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author 刘渠好
 * @since 2024-08-30 21:31
 * docker沙箱测试
 */

@SpringBootTest
public class JavaDockerCodeSandBoxTest {


    @Resource
    private CodeSandbox codeSandbox;


    @Test
    void testExecuteCode(){
        String code= ResourceUtil.readStr ("testcode/core/Main2.java", StandardCharsets.UTF_8);
        ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder ()
                .code (code)
                .language (QuestionSubmitLanguageEnum.JAVA)
                .inputList (Arrays.asList ("1 2", "3 4"))
                .build ();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode (codeRequest);
        System.out.println (executeCodeResponse);


    }
}
