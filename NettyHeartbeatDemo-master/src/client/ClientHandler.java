package client;

import common.PacketProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static common.PacketProto.Packet.newBuilder;

public class ClientHandler implements Runnable {

    private static Channel ch;
    private static Bootstrap bootstrap;

    @Override
    public void run() {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        final HashedWheelTimer timer = new HashedWheelTimer();
        try {
            bootstrap = new Bootstrap();

            bootstrap
                    // 绑定线程组
                    .group(workGroup)
                    // 设定IO通讯模式
                    .channel(NioSocketChannel.class)
                    //如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；
                    // 如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
                    .option(ChannelOption.TCP_NODELAY, true)
                    //添加客户端处理类, 可以类比成servlet和filter
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            // 添加一个超时处理器， 每五秒不发送请求就
                            pipeline.addLast(new IdleStateHandler(0, 5, 0));
                            // 定义了一个 HeartbeatServerHandler 处理器，用来处理超时时，发送心跳
                            pipeline.addLast(new ClientHeartbeatHandler(true, bootstrap, timer));


                        }
                    });


            // 连接服务器
            doConnect();

            // 模拟不定时发送向服务器发送数据的过程
            Random random = new Random();
            while (true) {
                int num = random.nextInt(21);
                Thread.sleep(num * 1000);
                PacketProto.Packet.Builder builder = newBuilder();
                builder.setPacketType(PacketProto.Packet.PacketType.DATA);
                builder.setData("我是数据包（非心跳包） " + num);
                PacketProto.Packet packet = builder.build();
                ch.writeAndFlush(packet);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        finally {
            workGroup.shutdownGracefully();
        }

    }


    /**
     * 抽取出该方法 (断线重连时使用)
     *
     * @throws InterruptedException
     */
    public static void doConnect() throws InterruptedException {
        ChannelFuture channel = bootstrap.connect("127.0.0.1", 20000);
        channel.addListener((ChannelFuture futureListener) -> {
            final EventLoop eventLoop = futureListener.channel().eventLoop();
            if(!futureListener.isSuccess()){
                // 3s 之后尝试重新连接服务器
                System.out.println("2 s 之后尝试重新连接服务器...");
                Thread.sleep(2 * 1000);
                eventLoop.schedule(new ClientHandler() ,10 , TimeUnit.SECONDS);
            }
        });
        ch =  channel.channel();
    }
}
