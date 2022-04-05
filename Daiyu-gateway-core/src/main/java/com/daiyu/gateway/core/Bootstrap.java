package com.daiyu.gateway.core;

public class Bootstrap {
    public static void main(String[] args) {
        //1.加载网关配置信息
        DaiyuGatewayConfig load = DaiyuConfigLoader.getInstance().load(args);

    }
}
