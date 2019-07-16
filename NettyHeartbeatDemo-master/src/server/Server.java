package server;

import common.PacketProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Yohann on 2016/11/9.
 */
public class Server {
    public static void main(String[] args) {
        NioEventLoopGroup acceptorGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(acceptorGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；
                    // 如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(PacketProto.Packet.getDefaultInstance()));
                            pipeline.addLast(new IdleStateHandler(6, 0, 0));
                            pipeline.addLast(new ServerHeartbeatHandler());
                        }
                    });
            Channel ch = bootstrap.bind(20000).sync().channel();
            System.out.println("Server has started...");

            ch.closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            acceptorGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
