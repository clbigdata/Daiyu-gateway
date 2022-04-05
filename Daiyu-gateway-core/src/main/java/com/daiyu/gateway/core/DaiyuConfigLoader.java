package com.daiyu.gateway.core;

import com.daiyu.common.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author ：chenlong
 * @filename DaiyuConfigLoader
 * @date 2022-04-05 10:13
 * @description：网关的配置信息加载类
 */
@Slf4j
public class DaiyuConfigLoader {

    private final static String CONFIG_FILE = "gateway.config";

    private final static String CONFIG_ENV_PREFIEX = "DAIYU_";

    private final static String CONFIG_JVM_PREFIEX = "daiyu.";


    private final static DaiyuConfigLoader INSTANCE = new DaiyuConfigLoader();

    private DaiyuGatewayConfig daiyuGatewayConfig = new DaiyuGatewayConfig();

    private DaiyuConfigLoader() {

    }

    public static DaiyuConfigLoader getInstance() {
        return INSTANCE;
    }

    public static DaiyuGatewayConfig getDaiyuConfig() {
        return INSTANCE.daiyuGatewayConfig;
    }

    public DaiyuGatewayConfig load(String args[]) {
        //加载逻辑
        //1.配置文件
        {
            InputStream resourceAsStream = DaiyuConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (resourceAsStream != null) {
                Properties properties = new Properties();
                try {
                    properties.load(resourceAsStream);
                    PropertiesUtils.properties2Object(properties, daiyuGatewayConfig);
                } catch (Exception e) {
                    log.warn("#DaiyuConfigLoader# load config file : {} is error", CONFIG_FILE, e);
                } finally {
                    if (resourceAsStream != null) {
                        try {
                            resourceAsStream.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }
        }
        //2.环境变量
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtils.properties2Object(properties, daiyuGatewayConfig, CONFIG_ENV_PREFIEX);

        }
        //3.JVM参数
        {
            Properties properties = System.getProperties();
            PropertiesUtils.properties2Object(properties, daiyuGatewayConfig, CONFIG_JVM_PREFIEX);
        }
        //4.运行参数 --xxx=xxx --enable=true
        {
            if (args != null && args.length != 0) {
                Properties properties = new Properties();
                for (String arg : args) {
                    if (arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtils.properties2Object(properties, daiyuGatewayConfig);
            }
        }

        return daiyuGatewayConfig;
    }
}
