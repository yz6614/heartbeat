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

import java.net.ConnectException;
import java.util.Random;

import static common.PacketProto.Packet.newBuilder;

/**
 * Created by Yohann on 2016/11/9.
 */
public class Client {





    public static void main(String[] args) {
       Thread initThd = new Thread( new ClientHandler());
        initThd.start();
    }


}
