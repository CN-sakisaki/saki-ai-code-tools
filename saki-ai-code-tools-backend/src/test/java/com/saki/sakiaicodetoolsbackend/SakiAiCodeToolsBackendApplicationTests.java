package com.saki.sakiaicodetoolsbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
class SakiAiCodeToolsBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[32]; // 256-bit secret
        random.nextBytes(secret);
        String secretKey = Base64.getEncoder().encodeToString(secret);
        System.out.println(secretKey);
    }

}
