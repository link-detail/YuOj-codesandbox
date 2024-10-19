# 创建一个容器
public CreateContainerResponse createContainer(String imageName, HostConfig config){
        return dockerClient.createContainerCmd (imageName)
                .withTty (false)
                .withNetworkDisabled (false)
                .withAttachStderr (true)
                .withAttachStdin (true)
                .withAttachStdout (true)
                .withReadonlyRootfs(true)
                .withHostConfig (config)
                .exec ();
    }

 解释说明：
 1.withTty(true): 是否为容器设立一个伪终端
 就是在容器内部，是否允许交互，比如python中的输入一个字符串代码 str = input() print(str) ，之后打印出来
 在允许伪终端的容器内部，这个str字符串是可以输出的，但是在不可以伪终端的容器内部，这个是不可以被输出的。

2.withNetworkDisabled (false)：是否和宿主机共享网络
当设置为false的时候，docker使用自己的网络堆栈，其中包括docker容器之间进行通信，设置容器别名，还有端口映射。要是设置为true的话，docker
就不在使用内部网络，而是和宿主机共享网络。

3..withAttachStdin(true) 允许你向容器内发送数据。
  .withAttachStdout(true) 允许你从容器获取标准输出数据。
  .withAttachStderr(true) 允许你从容器获取错误输出数据。
//注意：这个主要侧重于容器内部

4.withHostConfig (config):容器内部的根系统文件权限设置为只读，不可以进行其他操作


# 获取执行创建cmd命令请求
public ExecCreateCmdResponse executeCreateCmd(String containerId, String... cmd){
        return dockerClient
                .execCreateCmd (containerId)
                .withAttachStdin (true)  允许执行创建cmd命令中发送数据
                .withAttachStderr (true) 允许获取执行创建cmd命令中错误输出数据
                .withAttachStdout (true) 允许后获取执行创建cmd命令中标准输出数据
                .withTty(true) //注意：当这个设置为true的时候，容器的输出流被合并了，不在区分错误输出和标准输出
                .withCmd (cmd)
                .exec ();
//注意：这个主要侧重于获取执行创建cmd命令中的一些操作
}

# 拉取一个镜像
pullImageCmd.
exec (pullImageResultCallback)
/**
1.阻塞当前线程，直到镜像拉取操作完成。
2.确保在镜像拉取完成后再继续执行后续的代码。
3.如果拉取过程中出现错误，通过异常机制来处理错误情况。
*/
.awaitCompletion ();

# 执行启动执行cmd命令
dockerClient
.execStartCmd (execId)
/**
false:withDetach(false)：这个设置表示容器将会在非分离模式（non-detached mode）下运行。在这种模式下，容器的执行会阻塞当前线程，
直到容器中的命令执行完成或者容器退出。
true:withDetach(true)：这个设置表示容器将会在分离模式（detached mode）下运行。在这种模式下，容器会在后台运行，不会阻塞当前线程，
你的程序可以继续执行后续的代码而不需要等待容器内命令的执行结果。
*/
.withDetach (false)
.withTty (false)
.withStdIn (inputStream)
.exec (execStartResultCallback)
.awaitCompletion ();

# hostConfig

/**
config.withMemorySwap(0L);
这个方法设置了容器的交换空间（swap space）限制。交换空间是当物理内存（RAM）不足时，系统用来临时存储数据的硬盘空间。在这里，设置为 0L 表示不启用交换空间。
这意味着容器只能使用分配给它的物理内存，一旦物理内存用尽，容器内的进程可能会被杀掉，因为 Docker 不会使用交换空间来扩展容器的可用内存。

config.withCpuCount(1L);
这个方法设置了容器可以使用的 CPU 核心数。在这里，设置为 1L 表示容器被限制为只能使用一个 CPU 核心。这可以用来限制容器的 CPU 使用量，防止它占用过多的系统资源，
影响宿主机或其他容器的性能。

config.withMemory(100*1000*1000L);
1GB=1024MB 1MB=1024*1024(字节)
这个方法设置了容器可以使用的内存量。在这里，设置为 100*1000*1000L，即 100 MB（兆字节）。这意味着容器内的进程总共可以使用 100 MB 的内存。
如果容器的进程尝试使用超过这个限制的内存，Docker 将杀死那些超出内存限制的进程。
*/

# 为什么正常启动容器，容器不会退出，但是以debug方式调试，就会退出：将withTty设置为true即可解决这个问题
dockerClient.createContainerCmd (imageName)
.withTty (true)  
.withNetworkDisabled (false) 
.withAttachStdout (true)  
.withReadonlyRootfs(true) 
.exec ();
/**

当您在 Docker 中设置 .withTty(false) 时，您告诉 Docker 不要为容器分配一个伪终端（pseudo-TTY）。
这意味着，当您启动容器时，它不会期望一个交互式会话，容器内运行的进程将不会有一个控制终端。

如果您在启动容器时没有分配 TTY（即 .withTty(false)），并且容器的主进程是一个需要交互的命令（比如需要用户输入的命令），那么容器可能会在执行完命令后立即退出。
这是因为在没有 TTY 的情况下，容器内的进程无法从标准输入（STDIN）接收输入，如果它们依赖于交互式输入，就会在完成它们的任务后退出，导致容器停止运行。

另一方面，如果您使用 .withTty(true)，Docker 会为容器分配一个伪终端，允许您与容器内的进程进行交互。即使您没有附加到容器（即没有使用 docker attach），容器也会保持运行状态，
因为主进程会等待 TTY 的输入，这通常是一个阻塞调用。

当您以 debug 模式启动容器时，通常意味着您希望容器保持运行状态，以便您可以进行调试。如果您在没有 TTY 的情况下启动容器，并且容器的主进程很快就完成了执行，那么容器就会停止。
这就是为什么您看到容器在以 debug 模式启动后会自己停止的原因。
*/














