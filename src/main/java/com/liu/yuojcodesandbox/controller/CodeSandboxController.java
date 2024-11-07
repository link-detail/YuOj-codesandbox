package com.liu.yuojcodesandbox.controller;

import com.liu.yuojcodesandbox.core.java.JavaNativeCodeSandbox;
import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
public class CodeSandboxController {


    //防止被用户刷接口，根据判断请求头信息来决定是否返回执行结果

    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    /**
     *     @Resource
     *     private JavaNativeCodeSandbox nativeCodeSandbox;
     */
    @Resource
    private JavaNativeCodeSandbox nativeCodeSandbox;
    /**
     *  执行代码沙箱请求
     * @param executeCodeRequest @RequestBody：以json的方式去传输参数
     * @return
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                       HttpServletResponse response) {
        //获取请求头
        String header = request.getHeader (AUTH_REQUEST_HEADER);

        //key进行加密
        String md5Secret = DigestUtils.md5DigestAsHex (("liu" + AUTH_REQUEST_SECRET).getBytes ());
        //header可能为空，要是空的话会报NPE错误，所以为了避免，将其放在括号里
        if (!md5Secret.equals (header)) {
            //401：无权限 403：被禁止
            response.setStatus (403);
            return null;
        }
        return nativeCodeSandbox.executeCode (executeCodeRequest);

    }
}
