package com.liu.yuojcodesandbox.controller;

import com.liu.yuojcodesandbox.core.CodeSandboxFactory;
import com.liu.yuojcodesandbox.core.CodeSandboxTemplate;
import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/codesandbox")
public class CodeSandboxController {
    @PostMapping("/execute")
    public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest) throws IOException {
        CodeSandboxTemplate codeSandboxTemplate = CodeSandboxFactory.getInstance(executeCodeRequest.getLanguage());
        return codeSandboxTemplate.executeCode(executeCodeRequest);
    }
}
