package client;

import common.PacketProto.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import static common.PacketProto.Packet.newBuilder;

/**
 * Created by Yohann on 2016/11/9.
 */
public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter  {

    private volatile boolean reconnect = true;
    private int attempts;
    private final Bootstrap bootstrap;
    private final Timer timer;

    public ClientHeartbeatHandler(boolean reconnect, Bootstrap bootstrap, Timer timer) {
        this.reconnect = reconnect;
        this.bootstrap = bootstrap;
        this.timer = timer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Server is active ---");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Server is inactive ---");
        ctx.fireChannelInactive();
        ClientHandler.doConnect();
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 不管是读事件空闲还是写事件空闲都向服务器发送心跳包
            sendHeartbeatPacket(ctx);
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("连接出现异常");
    }

    /**
     * 发送心跳包
     *
     * @param ctx
     */
    private void sendHeartbeatPacket(ChannelHandlerContext ctx) {
        Packet.Builder builder = newBuilder();
        builder.setPacketType(Packet.PacketType.HEARTBEAT);
        Packet packet = builder.build();
        ctx.writeAndFlush(packet);
    }

}
