package com.liu.yuojcodesandbox.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 刘渠好
 * @since 2024-08-28 21:27
 * docker客户端配置
 */
@Configuration
@Slf4j
public class DockerClientConfig {

    @Bean
    public DockerClient dockerClient(){
        return DockerClientBuilder.getInstance ().build ();
    }
}
