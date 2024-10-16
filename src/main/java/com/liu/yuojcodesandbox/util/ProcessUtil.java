package com.liu.yuojcodesandbox.util;

import cn.hutool.core.util.StrUtil;
import com.liu.yuojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author 刘渠好
 * @since 2024/8/12 下午9:38
 * 进程处理工具类
 */
@Slf4j
public class ProcessUtil {


    /**
     * 进程运行
     */
    public static ExecuteMessage handleProcessMessage(Process process,String optName) {
        int exitValue;
        StringBuilder errorPut=new StringBuilder();
        StringBuilder outPut=new StringBuilder();

        try {
            //信息码
             exitValue = process.waitFor();
            //正常运行
            if (exitValue==0){
                log.info(optName+"成功!");
            }else {  //异常运行
                //获取控制台的错误信息输出
                log.error(optName + "失败，错误码:{}", exitValue);
                //获取错误信息输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                errorPut = new StringBuilder();
                String errorMessage;
                while ((errorMessage = bufferedReader.readLine()) != null) {
                    errorPut.append(errorMessage);
                }
                log.error("错误输出：{}", errorPut);
            }

                //获取正常信息输出
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outMessage;
                while ((outMessage=bufferedReader1.readLine())!=null){
                    outPut.append(outMessage);
                }
                if (StrUtil.isNotBlank(outPut)){
                    log.info("正常输出：{}",outPut);
                }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //返回执行信息
        return ExecuteMessage.builder()
                .message(outPut.toString())
                .errorMessage(errorPut.toString())
                .exitValue(exitValue)
                .build();
    }

    /**
     * 交互式
     */

    public static ExecuteMessage handleProcessInteraction(Process process,String input,String optName){
        OutputStream outputStream = process.getOutputStream();
        try {
            //todo 这个是怎么实现的
            byte[] bytes = (input+"\n").getBytes ();
            outputStream.write(bytes);  //控制台输入
            outputStream.flush();  //控制台按下回车键
            outputStream.close();
            return handleProcessMessage(process,optName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("关闭输出流失败!");
            }
        }
    }
}
