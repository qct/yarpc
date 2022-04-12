package org.yarpc.core.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.codec.ProtocolDecoder;
import org.yarpc.core.codec.ProtocolEncoder;
import org.yarpc.core.exception.RequestTimeoutException;
import org.yarpc.core.serializer.KryoSerializer;
import org.yarpc.core.transport.handler.YaRpcClientHandler;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class NettyClientTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientTransporter.class);
    private final int requestTimeoutMillis = 100 * 1000; // default 10seconds

    private final String ip;
    private final int port;

    private final Channel channel;

    public NettyClientTransporter(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.channel = bootstrapClient();
    }

    @Override
    public Response sendToRemote(Request request) {
        if (channel == null) {
            return Response.builder()
                .throwable(new RuntimeException("Channel is not available now"))
                .build();
        }
        channel.writeAndFlush(request);
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseHolder.put(request.getRequestId(), future);
        try {
            Response response = future.get(requestTimeoutMillis, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new RequestTimeoutException(
                    "service: " + request.getClazz().getName() + " method " + request.getMethod() + " timeout exceed "
                        + requestTimeoutMillis);
            }
            return response;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RequestTimeoutException(
                "service: " + request.getClazz().getName() + " method " + request.getMethod() + " timeout exceed "
                    + requestTimeoutMillis);
        } finally {
            ResponseHolder.remove(request.getRequestId());
        }
    }

    private Channel bootstrapClient() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1,
            new ThreadFactoryBuilder().setNameFormat("yaRpc-client-pool-%d").build());
        bootstrap.channel(NioSocketChannel.class)
            .group(eventLoopGroup)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline()
                        .addLast(new LoggingHandler(LogLevel.INFO)) //Duplex
                        .addLast(new ProtocolDecoder(10 * 1024 * 1024, new KryoSerializer())) // inbound
                        .addLast(new ProtocolEncoder(new KryoSerializer())) // outbound
                        .addLast(new YaRpcClientHandler()); // inbound
                }
            });

        try {
            final ChannelFuture channelFuture = bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.TCP_NODELAY, true)
                .connect(this.ip, this.port).sync();
            final Channel channel = channelFuture.channel();
            addChannelListeners(channelFuture, channel);
            return channel;
        } catch (InterruptedException e) {
            logger.error("getOrCreateChannel error", e);
        }
        return null;
    }

    private void addChannelListeners(ChannelFuture channelFuture, Channel channel) {
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.info("Connect success {} ", channelFuture);
                }
            }
        });
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                logger.info("Channel Close {} {}", ip, port);
            }
        });
    }
}
