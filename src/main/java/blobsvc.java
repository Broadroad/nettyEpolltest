import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class blobsvc {
    private final static int MAX_BUF_SIZE 					= 1024;
    private int blobCount;
    private InetSocketAddress serverAddr;
    public blobsvc(String ip, int port, int blobCount) {

        this.blobCount 	= blobCount;
        this.serverAddr 	= new InetSocketAddress(ip, port);
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

    private void doRun(){
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new blobsvc(port).run();
    }
}
