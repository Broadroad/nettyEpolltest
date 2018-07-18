import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


/**
 * Created by zhangk23 on 2018/3/30.
 */
public class NettyClient {
    public static String HOST = "10.249.248.192";
    public static int PORT = 8080;

    public static Bootstrap bootstrap = getBootstrap();
    public static Channel channel = getChannel(HOST, PORT);
    public static String TEXT = "1234567890asdfhasdfklashdfklashdfkjljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljljlah";

    /**
     * Bootstrap
     *
     * @return
     */
    public static final Bootstrap getBootstrap() {
        EventLoopGroup group = new EpollEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(EpollChannelOption.TCP_QUICKACK, true);
        b.group(group).channel(EpollSocketChannel.class);
        b.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast("handler", new NettyClientHandler());
            }
        });
        b.option(ChannelOption.SO_KEEPALIVE, true);
        return b;
    }

    public static final Channel getChannel(String host, int port) {
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            System.out.println("Connect Server failed " + host + ":" + port);
            e.printStackTrace();
            return null;
        }
        return channel;
    }

    public static void sendMsg(String msg) throws Exception {
        if (channel != null) {
            channel.writeAndFlush(msg).sync();
        } else {
            System.out.println("send msg failed, because connection not connected");
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            long t0 = System.nanoTime();
            for (int i = 0; ; i++) {
                sendMsg(i + TEXT);
            }
            //long t1 = System.nanoTime();
            //System.out.println((t1 - t0) / 1000000.0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
