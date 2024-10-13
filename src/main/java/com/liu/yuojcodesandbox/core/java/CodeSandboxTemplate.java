package com.liu.yuojcodesandbox.core.java;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.liu.yuojcodesandbox.core.CodeSandbox;
import com.liu.yuojcodesandbox.model.*;
import com.liu.yuojcodesandbox.model.enums.JudgeInfoMessageEnum;
import com.liu.yuojcodesandbox.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * @author 刘渠好
 * @since 2024/8/11 下午9:30
 * 代码沙箱模板
 */
@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {

    //存放路径前缀 /java
    String prefix;
    //代码存放路径 /tempCode
    String globalCodePath;
    //统一类名 /Main1.java
    String globalCodeFileName;


    //限制执行时间
    public static final Long TIME_OUT = 10000L;  //10s

    //保存代码方法
    public File saveCodeFile(String code) {
        String userPath = System.getProperty("user.dir");
        //tempCode文件夹存储代码
        String userParentPath = userPath + globalCodePath;
        if (!FileUtil.exist(userParentPath)) {
            FileUtil.mkdir(userParentPath);
        }
        //分层存放
        String userCodePath = userParentPath +prefix+ File.separator + UUID.randomUUID();
        //将代码写入指定位置
        String codePath = userCodePath + globalCodeFileName;
        File codeFile = FileUtil.writeString(code, codePath, StandardCharsets.UTF_8);
        return codeFile;
    }

    /**
     * 获取编译/运行代码命令方法
     * userCodePath  :编译文件使用(代码所在目录)
     * userCodeParentPth :运行文件使用(代码所在父目录)
     */
    abstract CodeSandboxCmd getCmd(String userCodePath, String userCodeParentPath);

    //编译代码
    public ExecuteMessage compileCode(String compileCmd) throws IOException {
        Process comProcess = Runtime.getRuntime().exec(compileCmd);
        return ProcessUtil.handleProcessMessage(comProcess,"编译");
    }

    //执行代码
    public List<ExecuteMessage> runCode(List<String> inputList, String runCmd) {
        List<ExecuteMessage> executeMessages = new LinkedList<>();  //存储信息
        for (String inputArgs : inputList) {

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //判断是否超时
                Thread computeTimeThread = new Thread(() -> {

                    try {
                        Thread.sleep(TIME_OUT);
                        //如果规定时间还没有结束
                        if (runProcess.isAlive()) {
                            log.info("超时了，中断!");
                            runProcess.destroy(); //销毁进程
                        }
                        executeMessages.add(
                                ExecuteMessage.builder()
                                        .errorMessage("超时")
                                        .time(TIME_OUT)
                                        .exitValue(-10001)
                                        .build()
                        );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                //启动超时监控线程
                computeTimeThread.start();
                StopWatch stopWatch = new StopWatch();  //计时
                stopWatch.start();
                ExecuteMessage executeMessage = ProcessUtil.handleProcessInteraction(runProcess, inputArgs, "运行");
                stopWatch.stop();
                computeTimeThread.stop();//关闭超时监控线程
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessages.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return executeMessages;
    }


    //原生代码沙箱执行代码
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        //1.保存代码
        File codeFile = saveCodeFile(code);
        String userCodePath = codeFile.getAbsolutePath();  //代码位置
        String userParentPath = codeFile.getParentFile().getAbsolutePath();// 每一个编译运行代码的位置
        CodeSandboxCmd cmdFromLanguage = getCmd(userCodePath, userParentPath);
        String compileCmd = cmdFromLanguage.getCompileCmd();  //编译命令
        String runCmd = cmdFromLanguage.getRunCmd(); //执行命令

        //2.编译代码
        try {
            ExecuteMessage executeMessage = compileCode(compileCmd);
            //编译失败
            if (executeMessage.getExitValue() != 0) {
                FileUtil.del(userParentPath);
                return ExecuteCodeResponse.builder()
                        .status(2)
                        .message("编译失败")
                        .resultType(JudgeInfoMessageEnum.COMPILE_ERROR)
                        .build();
            }
        } catch (IOException e) {
            //这里是确保删除成功，可能上面没删除
            FileUtil.del(userParentPath);
            return getErrorResponse(e);
        }

        //3.运行代码及其整理输出结果
        try {
            List<ExecuteMessage> executeMessages = runCode(inputList, runCmd);
            //返回处理结果
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(1);
            //程序判断执行信息
            executeCodeResponse.setResultType(JudgeInfoMessageEnum.ACCEPTED);
            JudgeInfo judgeInfo = new JudgeInfo();
            executeCodeResponse.setJudgeInfo(judgeInfo);
            //收集输出结果
            List<String> outputList = new LinkedList<>();
            long maxTime = 0;
            for (ExecuteMessage executeMessage : executeMessages) {
                if (ObjectUtil.equal(0, executeMessage.getExitValue())) {
                    outputList.add(executeMessage.getMessage());
                    //超时
                } else if (ObjectUtil.equal(-10001, executeMessage.getExitValue())) {
                    executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                    executeCodeResponse.setResultType(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED);
                    JudgeInfo timeJudgeInfo = new JudgeInfo();
                    timeJudgeInfo.setTime(executeMessage.getTime());
                    executeCodeResponse.setJudgeInfo(timeJudgeInfo);
                    executeCodeResponse.setStatus(3);
                    break;
                    //运行失败
                } else {
                    executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                    executeCodeResponse.setResultType(JudgeInfoMessageEnum.RUNTIME_ERROR);
                    executeCodeResponse.setStatus(3);
                    break;
                }
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }
                //设置程序执行时间
                judgeInfo.setTime(maxTime);
                executeCodeResponse.setOutputList(outputList);
                FileUtil.del(userParentPath);
                return executeCodeResponse;
        }catch (RuntimeException e){
            FileUtil.del(userParentPath);
            return getErrorResponse(e);
        }
    }

    public ExecuteCodeResponse getErrorResponse(Throwable e) {
        return ExecuteCodeResponse.builder()
                .outputList(new ArrayList<>())
                .status(2)
                .message(e.getMessage())
                .judgeInfo(new JudgeInfo())
                .resultType(JudgeInfoMessageEnum.SYSTEM_ERROR)
                .build();
    }

}
