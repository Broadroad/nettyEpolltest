import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class blobsvc {
    private final static int MAX_BUF_SIZE 					= 1024;
    private int blobCount;
    private String ip;
    private int port;
    protected ByteBufAllocator byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
    protected Class<? extends Channel> channelClass = EpollSocketChannel.class;
    protected volatile EpollEventLoopGroup eventLoopGroup;
    public Bootstrap bootstrap;
    private Channel channel;


    public blobsvc(String ip, int port, int blobCount) {

        this.blobCount 	= blobCount;
        this.ip = ip;
        this.port = port;

    }

    public void init() {
        if (eventLoopGroup == null) {
            synchronized(this) {
                if (eventLoopGroup == null) {
                    eventLoopGroup = new EpollEventLoopGroup(50);
                    eventLoopGroup.setIoRatio(80);
                }
            }
        }
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        bootstrap.option(ChannelOption.WRITE_SPIN_COUNT, 32);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, byteBufAllocator);
        bootstrap.option(EpollChannelOption.TCP_QUICKACK, true);

        bootstrap.channel(channelClass);
        bootstrap.group(eventLoopGroup);

        this.bootstrap = bootstrap;

    }

    private void sendMessageToSS(SocketChannel sockch, int clientNo, int index) throws IOException {
        ByteBuffer sendBuf = ByteBuffer.allocate(MAX_BUF_SIZE);
        String sendText = "Client " + clientNo + " say " + index + "\r\n";
        sendBuf.put(sendText.getBytes());
        sendBuf.flip();
        sockch.write(sendBuf);
        System.out.println(sendText);
    }

    private void recvMessage(SocketChannel sockch, int clientNo) throws IOException {
        ByteBuffer recvBuf = ByteBuffer.allocate(MAX_BUF_SIZE);
        int bytesRead = sockch.read(recvBuf);
        while (bytesRead > 0) {
            recvBuf.flip(); // write mode to read mode, position to 0, // limit to position
            String recvText = new String(recvBuf.array(), 0, bytesRead);
            recvBuf.clear(); // clear buffer content, read mode to write mode, position to 0, limit to capacity
            System.out.println("Client " + clientNo + " receive: " + recvText);
            bytesRead = sockch.read(recvBuf);
        }
    }

    public void startClient() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(blobCount);
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                SSPromise promise = null;
                                // Connect
                ChannelFuture channelFuture = bootstrap.connect(ip, port);

                try {
                    channelFuture.await();
                    if (channelFuture.isSuccess()){
                        channel = channelFuture.channel();
                        ByteBuf buf = Unpooled.buffer(1200);
                        for(int i=0; i<1200; i++){
                            buf.writeByte(i+1);
                        }
                        promise = new SSPromise(channel, buf);
                        promise.setChannelFuture(channel.writeAndFlush(buf));


                        int timeOut = 10;

                        try {

                            if (timeOut > 0) {
                                if (!promise.await(timeOut)) {
                                    System.out.println("timeout");
                                }
                            } else {
                                // no timeout
                                promise.await();
                            }

                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // loop to send request
                send3CopyRequest();
            }
        });
    }

    private void send3CopyRequest(){
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
    }
}
