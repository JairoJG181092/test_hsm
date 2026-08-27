package com.coltomex.test_hsm.filecrypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-crypto")
public record FileCryptoProperties(
        String rsaTransformation,
        int aesKeySize,
        int gcmTagBits,
        int gcmIvBytes,
        int bufferBytes,
        int parallelism,
        String outputExtension,
        long maxFileSize) {
}
