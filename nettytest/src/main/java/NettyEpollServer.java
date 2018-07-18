import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.simple.SimpleLogger;

import java.util.logging.Logger;


/**
 * Created by zhangk23 on 2018/3/30.
 */
public class NettyEpollServer {
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    static final String IP = "0.0.0.0";
    private static EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new EpollEventLoopGroup();

    public static void run() throws Exception{


            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(EpollChannelOption.TCP_QUICKACK, true);

            b.channel(EpollServerSocketChannel.class);
            b.group(bossGroup, workerGroup);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast(new NettyEpollServerHandler());
                }
            });
            b.bind(IP, PORT).sync();
    }

    public static void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        System.out.print("start server");
        run();
    }
}
