package be.tass.zmidisynth;

/**
 * Created by vinz on 14/02/14.
 */
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import org.zeromq.ZMQ;
import android.view.View;

public class ZeroMQSubscriber implements Runnable {
    private final Handler uiThreadHandler;
    private final String connectAddress;

    public ZeroMQSubscriber(Handler uiThreadHandler, String connectAddress) {
        this.uiThreadHandler = uiThreadHandler;
        this.connectAddress = connectAddress;
    }

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

        subscriber.connect(connectAddress);
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);

        while(!Thread.currentThread().isInterrupted()) {
            byte[] bMidi = subscriber.recv();
            Bundle b = new Bundle();
            Message msg = new Message();
            b.putByteArray("MIDIkey", bMidi);
            msg.setData(b);
            uiThreadHandler.sendMessage(msg);
        }
        subscriber.close();
        context.term();
    }
}