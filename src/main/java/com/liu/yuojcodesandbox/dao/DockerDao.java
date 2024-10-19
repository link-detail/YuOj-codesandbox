package com.liu.yuojcodesandbox.dao;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author 刘渠好
 * @since 2024-08-28 21:22
 * docker基本操作
 */
@Repository
@Slf4j
public class DockerDao {


    @Resource
    private DockerClient dockerClient;

    /**
     * 拉取镜像
     * @param imageName 镜像名字
     */
    public void pullImage(String imageName) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd (imageName);
        /**
         * try-with-resources 写法
         * try（） 括号里面的资源会在try块执行完毕之后关闭，不管是否会发生异常，都会自动被关闭（这种比基本的try catch 逻辑要好一点）
         */
        try(PullImageResultCallback pullImageResultCallback = new PullImageResultCallback () {
            @Override
            public void onNext(PullResponseItem item) {
                log.info ("拉取镜像:{}", item.getStatus ());
                super.onNext (item);
            }
        }) {
            pullImageCmd.
                    exec (pullImageResultCallback)
                    //等待结束之后继续往下进行
                    .awaitCompletion ();
           log.info ("{}镜像拉取成功",imageName);
        }catch (Exception e){
            log.error ("{}镜像拉取错误",imageName,e);
            throw new RuntimeException ("拉取镜像失败!");
        }

    }

    /**
     * 创建一个容器
     * @param imageName 容器基于的镜像
     * @param config 容器内部配置
     * @return 容器响应对象 .withReadonlyRootfs (true) //限制用户访问容器根目录权限为只读
     */
    public CreateContainerResponse createContainer(String imageName, HostConfig config){
        return dockerClient.createContainerCmd (imageName)
                .withTty (true) //允许用户和容器交互式输入输出
                .withNetworkDisabled (false) //容器和宿主机网络不可通
                .withAttachStderr (true)//允许你获取容器内的错误输出
                .withAttachStdin (true) //允许容器输出
                .withAttachStdout (true) //允许获取容器内的正常输出
                .withReadonlyRootfs(true) //容器的根文件系统设置为只读，防止容器内运行的进程修改文件系统
                .withHostConfig (config)
                .exec ();
    }

    /**
     * 启动容器
     * @param containerId 容器id
     */
    public void startContainer(String containerId){
        dockerClient.startContainerCmd (containerId).exec();
    }

    /**
     * 创建container执行cmd
     * @param containerId 容器id
     * @param cmd 命令
     * @return {@link ExecCreateCmdResponse}
     */
    public ExecCreateCmdResponse executeCreateCmd(String containerId, String... cmd){
        return dockerClient
                .execCreateCmd (containerId)
                .withAttachStdin (true)
                .withAttachStderr (true)
                .withAttachStdout (true)
                //设置这个属性true之后，输出流都被合并了，不在区分标准输出流和正常输出流
                .withTty(false)
                .withCmd (cmd)
                .exec ();
    }

    /**
     * 执行启动
     * @param execId 执行id
     * @param inputStream 输入流
     * @param execStartResultCallback 回调函数
     */
    public void executeStart(String execId, InputStream inputStream, ExecStartResultCallback execStartResultCallback){
        try {
            dockerClient
                    .execStartCmd (execId)
                    .withDetach (false)
                    .withTty (false)
                    .withStdIn (inputStream)
                    .exec (execStartResultCallback)
                    .awaitCompletion ();
        } catch (InterruptedException e) {
            throw new RuntimeException (e);
        }
    }


    /**
     * 执行启动（限制时间）
     * @param execId 执行id
     * @param inputStream 输入流
     * @param execStartResultCallback 回调函数
     * @param timeOut 超时时间
     * @param timeUnit 时间单位
     */
    public void executeStart(String execId, InputStream inputStream, ExecStartResultCallback execStartResultCallback,
                             long timeOut, TimeUnit timeUnit){
        try {
            dockerClient
                    .execStartCmd (execId)
                    .withExecId (execId)
                    .withTty (true)
                    .withStdIn (inputStream)
                    .exec (execStartResultCallback)
                    .awaitCompletion (timeOut,timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * 执行启动（限制时间）
     * @param execId 执行id
     * @param execStartResultCallback 回调函数
     * @param timeOut 超时时间
     * @param timeUnit 时间单位
     *
     */
    public void executeStart(String execId, ExecStartResultCallback execStartResultCallback,
                             long timeOut, TimeUnit timeUnit){
        try {
            dockerClient
                    .execStartCmd (execId)
                    //非分离模式，表示容器的执行会阻塞当前线程，等待容器内的执行结果
                    .withDetach(false)
                    .exec (execStartResultCallback)
                    .awaitCompletion (timeOut,timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * 删除容器
     * @param containerId 容器id
     */
    public void deleteContainer(String containerId){
        //withForce 强制删除
        dockerClient.removeContainerCmd (containerId).withForce (true).exec ();
    }

    /**
     * 获取容器状态
     * @param containerId  容器id
     * @param resultCallback 回调函数
     * @return {@link ResultCallback<Statistics>}
     */
    public ResultCallback<Statistics> getStats(String containerId,ResultCallback<Statistics> resultCallback){
        return dockerClient.statsCmd (containerId).exec (resultCallback);
    }

    /**
     * 删除镜像
     * @param imageId 镜像名字
     */

    public void  delImage(String imageId){
        dockerClient.removeImageCmd (imageId).withForce (true).exec ();
    }
}
