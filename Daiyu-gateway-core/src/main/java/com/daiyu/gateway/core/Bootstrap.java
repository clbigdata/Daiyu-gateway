package com.daiyu.gateway.core;

public class Bootstrap {
    public static void main(String[] args) {
        //1.加载网关配置信息
        DaiyuGatewayConfig gatewayConfig = DaiyuConfigLoader.getInstance().load(args);
        DaiyuContainer daiyuContainer = new DaiyuContainer(gatewayConfig);
        daiyuContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                daiyuContainer.shutdown();
            }
        }));
    }
}
