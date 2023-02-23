package com.kyxs.cloud.core.logging.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.net.InetAddress;

/**
 * @Author wangsf
 * @Date 2023/2/20
 * @Version 1.0.0
 */
public class LogIpConfig extends ClassicConverter {
    private static String webIP;
    static {
        try {
            webIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            webIP = null;
        }
    }
    @Override
    public String convert(ILoggingEvent event) {
        return webIP;
    }
}
