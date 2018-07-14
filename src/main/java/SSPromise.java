import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class SSPromise extends DefaultChannelPromise {
    private ByteBuf _responseMsg;
    private ChannelFuture _channelFuture;

    public SSPromise(Channel channel) {
        super(channel);
    }

    public SSPromise(Channel channel, ByteBuf readBuffer) {
        super(channel);

        if (readBuffer != null) {
            _responseMsg = readBuffer;
        }
    }

    public ByteBuf get_responseMsg() {
        return _responseMsg;
    }

    public void set_responseMsg(ByteBuf msg) {
        _responseMsg = msg;
    }

    public ChannelFuture getChannelFuture() {
        return _channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this._channelFuture = channelFuture;
    }

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        // A promise can be created without a channel during a failure
        // This prevents a null pointer exception
        if (isDone() && (channel() == null)) {
            try {
                ((GenericFutureListener)listener).operationComplete(this);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } else {
            super.addListener(listener);
        }

        return this;
    }

    @Override
    public ChannelPromise setSuccess() {
        ChannelPromise promise = setSuccess(null);
        return promise;
    }

    @Override
    public ChannelPromise setSuccess(Void result) {
        ChannelPromise promise = super.setSuccess(result);
        return promise;
    }

    @Override
    public ChannelPromise await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public boolean await(long timeOutMillis) throws InterruptedException {
        boolean result = super.await(timeOutMillis);
        return result;
    }
}
