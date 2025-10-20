package com.saki.sakiaicodetoolsbackend;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Arrays;


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
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void checkMapper() {
        String[] mappers = applicationContext.getBeanNamesForType(com.saki.sakiaicodetoolsbackend.mapper.UserMapper.class);
        System.out.println("UserMapper beans = " + Arrays.toString(mappers));
    }
    public static void main(String[] args) {

        SpringApplication.run(SakiAiCodeToolsBackendApplication.class, args);
    }

}
