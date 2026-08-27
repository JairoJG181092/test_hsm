package com.coltomex.test_hsm;

import com.coltomex.test_hsm.filecrypto.FileCryptoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = FileCryptoProperties.class)
@ComponentScan(basePackages = {
        "com.coltomex.test_hsm",
        "com.coltomex.arc.jwe",
        "com.coltomex.arc.common",
        "com.coltomex.arc.hsm"
})
public class TestHsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestHsmApplication.class, args);
    }
}
