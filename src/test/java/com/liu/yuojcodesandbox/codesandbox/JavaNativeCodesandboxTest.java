package com.liu.yuojcodesandbox.codesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.liu.yuojcodesandbox.core.java.CodeSandboxFactory;
import com.liu.yuojcodesandbox.core.java.CodeSandboxTemplate;
import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.liu.yuojcodesandbox.model.enums.QuestionSubmitLanguageEnum.JAVA;

/**
 * @author 刘渠好
 * @since 2024/8/12 下午11:15
 */
public class JavaNativeCodesandboxTest {

 @Test
 void testDemo1(){
  ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder()
          .inputList(Arrays.asList("1 2", "3 1"))
          .language(JAVA)
          .code(ResourceUtil.readStr("testcode/core/Main.java", StandardCharsets.UTF_8))
          .build();
  CodeSandboxTemplate codeSandboxTemplate = CodeSandboxFactory.getInstance(codeRequest.getLanguage());
  ExecuteCodeResponse executeCodeResponse = codeSandboxTemplate.executeCode(codeRequest);
  System.out.println(executeCodeResponse);

 }

}
