/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import java.net.InetSocketAddress;
import java.util.Map;

import org.redisson.RedissonFuture;
import org.redisson.client.handler.CommandBatchEncoder;
import org.redisson.client.handler.CommandDecoder;
import org.redisson.client.handler.CommandEncoder;
import org.redisson.client.handler.CommandsQueue;
import org.redisson.client.handler.ConnectionWatchdog;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.core.RFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class RedisClient {

    private final Bootstrap bootstrap;
    private final InetSocketAddress addr;
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final long timeout;
    private boolean hasOwnGroup;

    public RedisClient(String host, int port) {
        this(new NioEventLoopGroup(), NioSocketChannel.class, host, port, 60 * 1000);
        hasOwnGroup = true;
    }

    public RedisClient(EventLoopGroup group, Class<? extends SocketChannel> socketChannelClass, String host, int port, int timeout) {
        addr = new InetSocketAddress(host, port);
        bootstrap = new Bootstrap().channel(socketChannelClass).group(group).remoteAddress(addr);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addFirst(new ConnectionWatchdog(bootstrap, channels),
                    CommandEncoder.INSTANCE,
                    CommandBatchEncoder.INSTANCE,
                    new CommandsQueue(),
                    new CommandDecoder());
            }
        });

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        this.timeout = timeout;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    long getTimeout() {
        return timeout;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public RedisConnection connect() {
        try {
            ChannelFuture future = bootstrap.connect();
            future.syncUninterruptibly();
            return new RedisConnection(this, future.channel());
        } catch (Exception e) {
            throw new RedisConnectionException("Unable to connect to: " + addr, e);
        }
    }

    public RFuture<RedisConnection> connectAsync() {
        final RedissonFuture<RedisConnection> f = new RedissonFuture<>();
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    RedisConnection c = new RedisConnection(RedisClient.this, future.channel());
                    f.complete(c);
                } else {
                    f.completeExceptionally(future.cause());
                }
            }
        });
        return f;
    }

    public RedisPubSubConnection connectPubSub() {
        try {
            ChannelFuture future = bootstrap.connect();
            future.syncUninterruptibly();
            return new RedisPubSubConnection(this, future.channel());
        } catch (Exception e) {
            throw new RedisConnectionException("Unable to connect to: " + addr, e);
        }
    }

    public RFuture<RedisPubSubConnection> connectPubSubAsync() {
        final RedissonFuture<RedisPubSubConnection> f = new RedissonFuture<>();
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    RedisPubSubConnection c = new RedisPubSubConnection(RedisClient.this, future.channel());
                    f.complete(c);
                } else {
                    f.completeExceptionally(future.cause());
                }
            }
        });
        return f;
    }

    public void shutdown() {
        shutdownAsync().syncUninterruptibly();
        if (hasOwnGroup) {
            bootstrap.group().shutdownGracefully();
        }
    }

    public ChannelGroupFuture shutdownAsync() {
        return channels.close();
    }

    /**
     * Execute INFO SERVER operation.
     *
     * @return Map extracted from each response line splitting by ':' symbol
     */
    public Map<String, String> serverInfo() {
        try {
            return serverInfoAsync().join();
        } catch (Exception e) {
            throw new RedisConnectionException("Unable to retrieve server into from: " + addr, e);
        }
    }

    /**
     * Asynchronously execute INFO SERVER operation.
     *
     * @return A future for a map extracted from each response line splitting by
     * ':' symbol
     */
    public RFuture<Map<String, String>> serverInfoAsync() {
        final RedisConnection connection = connect();
        RFuture<Map<String, String>> async = connection.async(RedisCommands.SERVER_INFO);
        async.handle((r, cause) -> {
            connection.closeAsync();
            return null;
        });
        return async;
    }

    @Override
    public String toString() {
        return "[addr=" + addr + "]";
    }

}
