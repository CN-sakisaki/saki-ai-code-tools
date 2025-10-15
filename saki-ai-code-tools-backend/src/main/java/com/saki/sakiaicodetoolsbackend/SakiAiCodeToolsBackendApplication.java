package com.saki.sakiaicodetoolsbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * 启动类
 * @author saki酱
 * @version 1.0
 * @since 2025-10-15 18:40
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.saki.sakiaicodetoolsbackend.mapper")
public class SakiAiCodeToolsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakiAiCodeToolsBackendApplication.class, args);
    }

}
