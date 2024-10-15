package com.liu.yuojcodesandbox.MyCodeSandBox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import com.liu.yuojcodesandbox.core.CodeSandbox;
import com.liu.yuojcodesandbox.model.*;
import com.liu.yuojcodesandbox.model.enums.JudgeInfoMessageEnum;
import com.liu.yuojcodesandbox.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author 刘渠好
 * @since 2024-10-15 23:15
 * java原生代码沙箱
 */
@Slf4j
@SuppressWarnings ("ALL")
public abstract class MyJavaNativeCodeSandBoxTemplate implements CodeSandbox {

    //存放代码路径
     String CODE_TEMPLATE_PATH;

    //目录前缀
     String PREFIX_NAME;

    //指定文件名
     String MAIN_NAME;

    //获取执行命令代码路径
    protected abstract CodeSandboxCmd getCmd(String userCodePath, String userCodeParentPath);

    /**
     * 保存代码
     * @param code 代码
      */
    public File saveCode(String code){
        //获取当前目录
        String  parentPath = System.getProperty ("user.dir");
        //存放代码文件
        String codeParentPath= parentPath+CODE_TEMPLATE_PATH;
        if (!FileUtil.exist (codeParentPath)){
            FileUtil.mkdir (codeParentPath);
        }
        //分层存放代码
        String codePrefixPath = codeParentPath+PREFIX_NAME+File.separator+ UUID.randomUUID ();
        //将代码写入指定文件
        String codePath=codePrefixPath+MAIN_NAME;
        return FileUtil.writeString (code, codePath, StandardCharsets.UTF_8);

    }

    /**
     * 编译代码
     * @param compileCmd 编译命令
     * @param optName 选择编译还是运行
     */
    public ExecuteMessage compileProcessCmd(String compileCmd, String optName) throws IOException {
        Process compileProcess = Runtime.getRuntime ().exec (compileCmd);
        return ProcessUtil.handleProcessMessage (compileProcess,optName);
    }

    /**
     * 运行代码
     * @param runcmd 运行命令
     * @param list 输入用例
     * @param optName 选择编译还是运行
     */
    public List<ExecuteMessage> runProcessCmd(String runcmd,List<String> list,String optName) throws IOException {
        LinkedList<ExecuteMessage> executeMessages = new LinkedList<> ();
        //TODO 这里需要注意一下
        Process runProcess = Runtime.getRuntime ().exec (runcmd);
        for (String s : list) {
            //统计时间
            StopWatch stopWatch = new StopWatch ();
            stopWatch.start ();
            ExecuteMessage executeMessage = ProcessUtil.handleProcessInteraction (runProcess, s, optName);
            stopWatch.stop ();
            long time = stopWatch.getLastTaskTimeMillis ();
            executeMessage.setTime (time);
            executeMessages.add (executeMessage);
        }
        return executeMessages;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //1.保存代码
        List<String> inputList = executeCodeRequest.getInputList ();
        String code = executeCodeRequest.getCode ();

        File codeFile = saveCode (code);
        String codePath = codeFile.getAbsolutePath ();
        String codeParentPath = codeFile.getAbsoluteFile ().getAbsolutePath ();
        CodeSandboxCmd cmd = getCmd (codePath, codeParentPath);
        String compileCmd = cmd.getCompileCmd (); //编译命令
        String runCmd = cmd.getRunCmd (); //执行命令

        //2.编译代码
        try {
            ExecuteMessage executeMessage = compileProcessCmd (compileCmd, "编译");
            //编译失败
            if (executeMessage.getExitValue ()!=0){
                //删除文件
                FileUtil.del (codeParentPath);
                return ExecuteCodeResponse
                        .builder ()
                        .message ("编译失败")
                        .status (2)
                        .resultType (JudgeInfoMessageEnum.COMPILE_ERROR)
                        .build ();
            }
        } catch (IOException e) {
            //删除文件
            FileUtil.del (codeParentPath);
            return getErrorResponse (e);
        }
        //3.运行代码并整理输出结果
        try {
            List<ExecuteMessage> messageList = runProcessCmd (runCmd, inputList, "运行");
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse ();
            executeCodeResponse.setResultType (JudgeInfoMessageEnum.ACCEPTED);
            executeCodeResponse.setStatus (1);
            JudgeInfo judgeInfo = new JudgeInfo ();
            executeCodeResponse.setJudgeInfo (judgeInfo);
            //收集输出结果
            LinkedList<String> outputList = new LinkedList<> ();
            //最大时间
            long max_time= 0;
            for (ExecuteMessage executeMessage : messageList) {
                if (executeMessage.getExitValue ()==0){
                    outputList.add (executeMessage.getMessage ());
                }//超时
                else if (executeMessage.getExitValue ()==100051){
                    break;
                }else {
                    executeCodeResponse.setMessage (JudgeInfoMessageEnum.RUNTIME_ERROR.getText ());
                    executeCodeResponse.setResultType (JudgeInfoMessageEnum.COMPILE_ERROR);
                    executeCodeResponse.setStatus (3);
                    break;
                }
                //todo 这里为什么这么写 时间不是只有两个吗
                max_time=Math.max (max_time,executeMessage.getTime ());
            }

            judgeInfo.setTime (max_time);
            executeCodeResponse.setOutputList (outputList);
            //清理文件
            FileUtil.del (codeParentPath);
            return executeCodeResponse;
        } catch (IOException e) {
            FileUtil.del (codeParentPath);
            return getErrorResponse (e);
        }
    }

    //4.增强程序健壮性
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        return ExecuteCodeResponse.builder ()
                .message (e.getMessage ())
                .judgeInfo (new JudgeInfo ())
                .outputList (new ArrayList<> ())
                .status (2)
                .resultType (JudgeInfoMessageEnum.SYSTEM_ERROR)
                .build ();
    }
}
