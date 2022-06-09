package com.zc.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.zc.gulimall.thirdparty.component.SmsConponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThridPartyApplicationTests {

    @Autowired
    OSSClient ossClient;
    @Autowired
    SmsConponent smsConponent;

    @Test
    void contextLoads() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("C:\\Users\\sampl\\Desktop\\crown.jpg");

        ossClient.putObject("zhaocan-gulimall", "crown.jpg", inputStream);

        ossClient.shutdown();

        System.out.println("上传成功");
    }

    @Test
    void testSendCode() {
        smsConponent.sendSmsCode("13341499179", "123456");
    }
}
