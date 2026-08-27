package com.coltomex.test_hsm.filecrypto;

import java.nio.file.Path;

public record FileCryptoResult(
        Path source,
        Path target,
        String operation,
        long bytesProcessed,
        String status,
        String error) {

    public static FileCryptoResult success(Path source, Path target, String operation, long bytes) {
        return new FileCryptoResult(source, target, operation, bytes, "SUCCESS", null);
    }

    public static FileCryptoResult failure(Path source, String operation, Exception error) {
        return new FileCryptoResult(source, null, operation, 0L, "FAILED", error.getMessage());
    }
}
