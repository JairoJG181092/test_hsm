package com.coltomex.arc.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log4J2Util {

    private static final Logger log = LogManager.getLogger(Log4J2Util.class);

    public static void info(String clazzName, String transactionId, String message) {
        log.info("{} - [{}] - {}", clazzName, transactionId, message);
    }

    public static void info(String clazzName, String transactionId, String message, Long initialTime) {
        log.info("{} - [{}] - {} :: {}", clazzName, transactionId, message,
                System.currentTimeMillis() - initialTime);
    }

    public static void error(String clazzName, String transactionId, String message) {
        log.error("{} - [{}] - {}", clazzName, transactionId, message);
    }
}