package com.saki.sakiaicodetoolsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class SakiAiCodeToolsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakiAiCodeToolsBackendApplication.class, args);
    }

}
