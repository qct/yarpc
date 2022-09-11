package org.yarpc.core.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.codec.ProtocolDecoder;
import org.yarpc.core.codec.ProtocolEncoder;
import org.yarpc.core.serializer.KryoSerializer;
import org.yarpc.core.transport.handler.YaRpcServerHandler;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class ServerImpl implements Server {

    private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private volatile boolean started = false;
    private final int port;

    private final String serviceName;
    private final Object serviceImpl;

    public ServerImpl(int port, String serviceName, Object serviceImpl) {
        this.port = port;
        this.serviceName = serviceName;
        this.serviceImpl = serviceImpl;
    }

    @Override
    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(
                1,
                new ThreadFactoryBuilder()
                        .setNameFormat(YaRpcConstant.SERVER_BOSS_POOL_NAME)
                        .setDaemon(true)
                        .build());
        this.workerGroup = new NioEventLoopGroup(
                YaRpcConstant.DEFAULT_IO_THREADS,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(YaRpcConstant.SERVER_WORKER_POOL_NAME)
                        .build());
        serverBootstrap
                .group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.INFO))
                                .addLast(new ProtocolDecoder(new KryoSerializer()))
                                .addLast(new ProtocolEncoder(new KryoSerializer()))
                                .addLast(new YaRpcServerHandler(serviceImpl));
                    }
                });
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            this.started = true;
            logger.info("Server started At {}", port);
            this.channel = channelFuture.channel();
        } catch (InterruptedException e) {
            logger.error("Server failed to start! ", e);
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down server {}", serviceName);
        this.started = false;
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        logger.info("Server shutdown successfully!");
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
