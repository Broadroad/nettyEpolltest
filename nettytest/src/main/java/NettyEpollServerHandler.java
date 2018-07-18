import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * Created by zhangk23 on 2018/3/30.
 */
public class NettyEpollServerHandler extends SimpleChannelInboundHandler <Object>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.print("Server recv message: " + msg);

        ctx.channel().writeAndFlush("yes, server is accepted you, nice" + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.print("Unexpected exception from downstream."+ cause);
        ctx.close();
    }
}
