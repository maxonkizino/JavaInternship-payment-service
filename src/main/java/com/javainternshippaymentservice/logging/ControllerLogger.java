package com.javainternshippaymentservice.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ControllerLogger {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogger.class);

    public void methodCalled(String controller, String method, Object... args) {
        if (args == null || args.length == 0) {
            log.info("{}.{} called", controller, method);
            return;
        }
        log.info("{}.{} called with args={}", controller, method, Arrays.toString(args));
    }
}
