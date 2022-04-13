package org.yarpc.core.transport;

import static org.yarpc.core.transport.ClientImpl.RESPONSE_HOLDER;

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
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.core.YaRpcConstant;
import org.yarpc.core.codec.ProtocolDecoder;
import org.yarpc.core.codec.ProtocolEncoder;
import org.yarpc.core.exception.ClientSideException;
import org.yarpc.core.exception.RequestTimeoutException;
import org.yarpc.core.serializer.KryoSerializer;
import org.yarpc.core.transport.handler.YaRpcClientHandler;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class NettyClientTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientTransporter.class);

    private final String ip;
    private final int port;
    private final Channel channel;

    public NettyClientTransporter(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.channel = bootstrapClient();
    }

    public NettyClientTransporter(Channel channel) {
        this.channel = channel;
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        this.port = remoteAddress.getPort();
        this.ip = remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public Response sendToRemote(Request request, long timeout) {
        if (channel == null) {
            throw new ClientSideException("Channel is not available now");
        }
        channel.writeAndFlush(request);
        CompletableFuture<Response> future = new CompletableFuture<>();
        RESPONSE_HOLDER.put(request.getRequestId(), future);
        try {
            Response response = future.get(timeout, TimeUnit.MILLISECONDS);
            if (response.getResp() == null || response.getE() != null) {
                throw response.getE().toException();
            }
            return response;
        } catch (InterruptedException | ExecutionException e) {
            String msg = "waiting for response Exception, service: " + request.getClazz().getName() + " method "
                + request.getMethod();
            logger.warn(msg);
            throw new ClientSideException(msg);
        } catch (TimeoutException e) {
            String msg =
                "service " + request.getClazz().getName() + "." + request.getMethod() + "(...) timeout exceed "
                    + timeout + " ms";
            logger.warn(msg);
            throw new RequestTimeoutException(msg);
        } finally {
            RESPONSE_HOLDER.remove(request.getRequestId());
        }
    }

    private Channel bootstrapClient() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1,
            new ThreadFactoryBuilder().setNameFormat(YaRpcConstant.CLIENT_WORKER_POOL_NAME).build());
        bootstrap.channel(NioSocketChannel.class)
            .group(eventLoopGroup)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline()
                        .addLast(new LoggingHandler(LogLevel.INFO)) //Duplex
                        .addLast(new ProtocolDecoder(new KryoSerializer())) // inbound
                        .addLast(new ProtocolEncoder(new KryoSerializer())) // outbound
                        .addLast(new YaRpcClientHandler()); // inbound
                }
            });

        try {
            final ChannelFuture channelFuture = bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.TCP_NODELAY, true)
                .connect(this.ip, this.port);
            final Channel channel = channelFuture.channel();
            addChannelListeners(channelFuture, channel);
            channelFuture.sync();
            return channel;
        } catch (Exception e) {
            logger.error("getOrCreateChannel error", e);
        }
        return null;
    }

    private void addChannelListeners(ChannelFuture channelFuture, Channel channel) {
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.info("Connect success {}", channelFuture);
                } else {
                    logger.error("Connect failed {}", channelFuture.cause().getMessage());
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
