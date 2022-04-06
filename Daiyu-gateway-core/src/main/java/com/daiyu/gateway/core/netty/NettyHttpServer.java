package com.daiyu.gateway.core.netty;

import com.daiyu.common.util.RemotingHelper;
import com.daiyu.common.util.RemotingUtil;
import com.daiyu.gateway.core.DaiyuGatewayConfig;
import com.daiyu.gateway.core.LifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author ：chenlong
 * @filename NettyHttpServer
 * @date 2022-04-05 11:00
 * @description：netty的服务端
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final DaiyuGatewayConfig daiyuGatewayConfig;

    private int port = 8888;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup eventLoopGroupBoss;

    private EventLoopGroup eventLoopGroupWork;

    public NettyHttpServer(DaiyuGatewayConfig daiyuGatewayConfig) {
        this.daiyuGatewayConfig = daiyuGatewayConfig;
        if (daiyuGatewayConfig.getPort() > 0 && daiyuGatewayConfig.getPort() < 65535) {
            this.port = daiyuGatewayConfig.getPort();
        }
        //初始化nettyHttpServer
        init();
    }


    /**
     * 初始化
     */
    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEPoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(daiyuGatewayConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossEpoll"));
            this.eventLoopGroupWork = new EpollEventLoopGroup(daiyuGatewayConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("NettyWorkEpoll"));
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(daiyuGatewayConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossNio"));
            this.eventLoopGroupWork = new NioEventLoopGroup(daiyuGatewayConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("NettyWorkNio"));
        }
    }

    public boolean useEPoll() {
        return daiyuGatewayConfig.isUseEPoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    /**
     * 服务启动
     */
    @Override
    public void start() {
        ServerBootstrap handler = this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWork)
                .channel(useEPoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)//sync+accept=1024
                .option(ChannelOption.SO_REUSEADDR, true)//TCP重绑定端口
                .option(ChannelOption.SO_KEEPALIVE, false)//如果在两小时内没有数据通信，TCP会自动
                .option(ChannelOption.TCP_NODELAY, true)//禁用Nagle算法，使用小数据传输时合并
                .option(ChannelOption.SO_SNDBUF, 65535)//设置发送缓冲区接受大小
                .option(ChannelOption.SO_RCVBUF, 65535)//设置接收缓冲区接受大小
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new HttpServerCodec(),
                                        new HttpObjectAggregator(daiyuGatewayConfig.getMacContentLength()),
                                        new HttpServerExpectContinueHandler(),
                                        new NettyServerConnectManagerHandler(),
                                        new NettyHttpServerHandler());
                    }
                });
        //判断是否需要内存分配
        if (daiyuGatewayConfig.isNettyAllocator()) {
            handler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        //启动
        try {
            this.serverBootstrap.bind().sync();
            log.info("<======= GateWay Sevrer StartUp on port" + this.port + "=====>");
        } catch (Exception e) {
            throw new RuntimeException(" this.serverBootstrap.bind().sync() fail!", e);
        }
    }

    @Override
    public void shutdown() {
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWork != null) {
            eventLoopGroupWork.shutdownGracefully();
        }
    }

    /**
     * 链接管理器handler
     */
    static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipline:channelRegistered {}", remoteAddr);
            super.channelRegistered(ctx);
        }


        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipline:channelUnregistered {}", remoteAddr);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipline:channelActive {}", remoteAddr);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipline:channelInactive {}", remoteAddr);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                //当没有数据发送和接收一段时间后的处理工作
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("Netty server pipline:userEventTriggered {}", remoteAddr);
                    ctx.channel().close();
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.warn("Netty server pipline:remoteAddr:{} ,exceptionCaught {}", remoteAddr, cause);
            ctx.channel().close();
        }
    }
}
