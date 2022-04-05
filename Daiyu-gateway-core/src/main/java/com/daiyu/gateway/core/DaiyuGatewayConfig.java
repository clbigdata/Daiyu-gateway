package com.daiyu.gateway.core;

import com.daiyu.common.constants.BasicConst;
import com.daiyu.common.constants.DaiyuBufferHelper;
import com.daiyu.common.util.NetUtils;
import lombok.Data;

/**
 * @author chenlong
 * @filename DaiyuGatewayConfig
 * @description 网关的通用配置信息类
 * @date 2022-04-04 10:50:00
 */
@Data
public class DaiyuGatewayConfig {

    //网关的默认端口
    private int port = 8888;

    //网关服务唯ID
    private String gatewayId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

    //网关的注册中心
    private String registerAddress = "";

    //网关的命名空间：dev test prod
    private String nameSpace = "daiyu-dev";

    private int processThread = Runtime.getRuntime().availableProcessors();

    //netty的Boss线程数
    private int eventLoopGroupBossNum = 1;

    //netty的word线程数
    private int eventLoopGroupWorkNum = processThread;

    //是否开启epoll
    private boolean useEPoll = true;

    //是否开启内存分配
    private boolean nettyAllocator = true;

    //http报纹大小
    private int macContentLength = 64 * 1024 * 1024;

    //httpAsync参数选项

    //dubbo开启链接数量
    private int dubboConnections = processThread;

    private boolean whenComplete = true;

    //网关队列缓冲模式
    private String bufferType = DaiyuBufferHelper.FLUSHER;

    //网关队列：阻塞/等待策略
    private String waitStrategy = "blocking";

    //TODO：带配置完成

}
