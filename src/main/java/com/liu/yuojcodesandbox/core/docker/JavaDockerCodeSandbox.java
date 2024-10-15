package com.liu.yuojcodesandbox.core.docker;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.liu.yuojcodesandbox.core.CodeSandbox;
import com.liu.yuojcodesandbox.dao.DockerDao;
import com.liu.yuojcodesandbox.model.ExecuteCodeRequest;
import com.liu.yuojcodesandbox.model.ExecuteCodeResponse;
import com.liu.yuojcodesandbox.model.ExecuteMessage;
import com.liu.yuojcodesandbox.model.JudgeInfo;
import com.liu.yuojcodesandbox.model.enums.JudgeInfoMessageEnum;
import com.liu.yuojcodesandbox.model.enums.LanguageImageEnum;
import com.liu.yuojcodesandbox.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author 刘渠好
 * @since 2024-08-28 22:21
 * docker沙箱
 */
@Service
@Slf4j
public class JavaDockerCodeSandbox implements CodeSandbox {

    //代码前缀
    public static final String PREFIX= File.separator+"java";

    //存放代码
    public static final String GLOBAL_CODE_DIR_PATH=File.separator+"tempCode";

    //统一类名
    public static final String GLOBAL_JAVA_CLASS_NAME=File.separator+"Main2.java";

    //超时时间 S
    public static final long TIME_OUT=5L;


    /**
     * 第一次拉取镜像
     */
    private static boolean FIRST_PULL = true;

    @Resource
    private DockerDao dockerDao;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList ();
        String code = executeCodeRequest.getCode ();

        //1.存放用户代码
        String globalCodePath = System.getProperty ("user.dir") + GLOBAL_CODE_DIR_PATH;
        if (!FileUtil.exist (globalCodePath)){
            FileUtil.mkdir (globalCodePath);
        }
        //分层存放
        String userCodeParentPath=globalCodePath+ PREFIX+File.separator+UUID.randomUUID ();
        String userCodePath= userCodeParentPath+GLOBAL_JAVA_CLASS_NAME;
        File codeFile = FileUtil.writeString (code, userCodePath, StandardCharsets.UTF_8);


        //2.编译代码
        try {
            String compileCmd = String.format ("javac -encoding utf-8 %s", codeFile.getAbsolutePath ());
            Process compileProcess = Runtime.getRuntime ().exec (compileCmd);
            ExecuteMessage executeMessage = ProcessUtil.handleProcessMessage(compileProcess, "编译");
            if (executeMessage.getExitValue()!=0){
                return getErrorResponse(new RuntimeException("编译失败!"));
            }
        } catch (IOException e) {
            return getErrorResponse (e);
        }

        //执行代码
        String imageName = LanguageImageEnum.JAVA11.getImage ();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse ();
        //默认是成功的
        executeCodeResponse.setResultType(JudgeInfoMessageEnum.ACCEPTED);
        executeCodeResponse.setStatus(1);
        if (FIRST_PULL){
            try {
                dockerDao.pullImage (imageName);
                FIRST_PULL=false;
            }catch (Exception e){
                throw new RuntimeException (e);
            }
        }

        //容器配置
        HostConfig config = new HostConfig ();
        config.withMemorySwap (0L); //没有交互空间
        config.withCpuCount (1L); //cpu数量
        config.withMemory (100*1000*1000L); //容器内存
        config.setBinds (new Bind (userCodeParentPath,new Volume ("/app")));  //目录挂载
        //创建容器
        CreateContainerResponse containerResponse = dockerDao.createContainer (imageName, config);
        String containerId = containerResponse.getId ();

        //启动容器
        dockerDao.startContainer (containerId);

        //是否超时
        final boolean[] timeout={true};
        //内存
        final long[] maxMemory = {0};
        //时间
        long maxTime=0;

        LinkedList<String> outPutList = new LinkedList<> ();
        for (String s : inputList) {
            String[] cmd = ArrayUtil.append (new String[]{"java", "-Dfile.encoding=utf-8", "-cp", "/app", "Main"}, s.split (" "));
//            String[] cmd=new String[]{"java", "-Dfile.encoding=utf-8", "-cp", "/app", "Main"};
            //创建容器执行命令
            String execId = dockerDao.executeCreateCmd(containerId, cmd).getId ();
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream (); //记录正常输出信息
            ByteArrayOutputStream errorResultStream = new ByteArrayOutputStream (); //记录错误输出信息
            //执行启动命令行回调函数
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback () {

                @Override
                public void onComplete() {
                    timeout[0]=false;
                    super.onComplete ();
                }

                //整理输出信息
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType (); //输出信息类型
                    byte[] payload = frame.getPayload ();  //输出信息
                    if (StreamType.STDERR.equals (streamType)){
                        try {
                            errorResultStream.write (payload);
                        } catch (IOException e) {
                            throw new RuntimeException (e);
                        }
                    }else {
                        try {
                            resultStream.write (payload);
                        } catch (IOException e) {
                            throw new RuntimeException (e);
                        }
                    }
                    super.onNext (frame);
                }
            };

            //容器状态回调函数
            ResultCallback<Statistics> resultCallback = new ResultCallback<Statistics> () {

                @Override
                public void close(){

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                //查看内存使用情况
                @Override
                public void onNext(Statistics statistics) {
                    Long usage = statistics.getMemoryStats ().getUsage ();
                    if (usage==null){
                        usage=0L;
                    }
                    log.info ("内存占用：{}",usage);
                    maxMemory[0]=Math.max (maxMemory[0],usage);
                }


                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            };

            try(ResultCallback<Statistics> ignored = dockerDao.getStats (containerId,resultCallback)) {
                StopWatch stopWatch = new StopWatch ();
                stopWatch.start ();
//                dockerDao.executeStart (execId, IoUtil.toStream(s+"\n",StandardCharsets.UTF_8),execStartResultCallback);
                /**
                 *  虽然限制了执行时间，但是不管是超时还是不超时，都会继续往下走的，不会报错
                 *  这里的时间限制，如果程序的执行耗时在限制时间之内，就是有正常输出的，如果超出这个限制时间，是没有输出的
                 */
                dockerDao.executeStart(execId,execStartResultCallback,TIME_OUT, TimeUnit.SECONDS);
                stopWatch.stop ();
                maxTime = Math.max (maxTime, stopWatch.getLastTaskTimeMillis ());
                if (resultStream.size () != 0) {
                    log.info ("正常输出:{}", resultStream);
                }
                if (errorResultStream.size () != 0) {
                    log.info ("错误输出:{}", errorResultStream);
                    executeCodeResponse.setStatus(3);
                    executeCodeResponse.setMessage(errorResultStream.toString());
                    executeCodeResponse.setResultType(JudgeInfoMessageEnum.RUNTIME_ERROR);
                    break;
                }
                outPutList.add (resultStream.toString ());

            }catch (IOException e){
                log.error ("执行失败",e);
            }
            log.info ("是否超时:{}",timeout[0]);
        }

        if (timeout[0]){
            executeCodeResponse.setOutputList(new ArrayList<>());
            executeCodeResponse.setStatus(2);
            executeCodeResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getText());
            executeCodeResponse.setResultType(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED);
        }
        //删除执行代码文件
        FileUtil.del (userCodeParentPath);
        JudgeInfo judgeInfo = new JudgeInfo ();
        judgeInfo.setTime (maxTime);
        judgeInfo.setMemory (maxMemory[0]);
        executeCodeResponse.setJudgeInfo (judgeInfo);
//        executeCodeResponse.setStatus (1);
        executeCodeResponse.setOutputList (outPutList);
//        executeCodeResponse.setResultType (JudgeInfoMessageEnum.ACCEPTED);

        //删除容器
        dockerDao.deleteContainer(containerId);

        return executeCodeResponse;
    }

    //增强代码健壮性
    public ExecuteCodeResponse getErrorResponse(Throwable e){

        return ExecuteCodeResponse.builder ()
                .outputList (new ArrayList<> ())
                .status (3)
                .message (e.getMessage ())
                .judgeInfo (new JudgeInfo ())
                .resultType (JudgeInfoMessageEnum.SYSTEM_ERROR)
                .build ();

    }
}
