package com.kyxs.cloud.core.logging.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Author wangsf
 * @Date 2023/02/20
 * @Version 1.0.0
 */
public class LogPortConfig extends ClassicConverter {
    private static String webPort;

    static {
        try {
            List<MBeanServer> serverList = MBeanServerFactory.findMBeanServer(null);
            for (MBeanServer server : serverList) {
                Set<ObjectName> names = new HashSet<ObjectName>();
                names.addAll(server.queryNames(new ObjectName("Catalina:type=Connector,*"), null));
                Iterator<ObjectName> it = names.iterator();
                while (it.hasNext()) {
                    ObjectName oName = (ObjectName) it.next();
                    String pValue = (String) server.getAttribute(oName, "protocol");
                    if (StringUtils.equals("HTTP/1.1", pValue)) {
                        webPort = ObjectUtils.toString(server.getAttribute(oName, "port"));
                    }
                }
            }
        } catch (Exception e) {
            webPort = null;
        }
    }

    @Override
    public String convert(ILoggingEvent event) {
        return webPort;
    }
}
