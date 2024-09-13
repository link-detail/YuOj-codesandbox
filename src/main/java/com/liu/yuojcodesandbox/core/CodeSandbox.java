package com.liu.yuojcodesandbox.core;

import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;



/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest  执行代码请求参数
     * @return 请求响应
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
