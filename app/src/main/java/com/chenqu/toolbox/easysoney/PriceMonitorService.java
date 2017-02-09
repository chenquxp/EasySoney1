package com.chenqu.toolbox.easysoney;

/**
 * Created by chenqu on 2017/2/9.
 */
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
public class PriceMonitorService  extends Service {
    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */

    private Timer timer;
    private int intival;
    private boolean once;
    private boolean isServiceRunning;
    private String exurl = "http://hq.sinajs.cn/list=sz159920";
    private String neturl = "http://hq.sinajs.cn/list=f_159920";
    private String tarurl = "http://hq.sinajs.cn/list=hkHSI";
    Handler timerhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            new Thread(networkTask).start();
        }
    };
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.putExtra("exvalue", GetHttpText(exurl));
            intent.putExtra("netvalue", GetHttpText(neturl));
            intent.putExtra("tarvalue", GetHttpText(tarurl));
            intent.setAction("com.chenqu.toolbox.easysoney.PriceMonitorService");
            sendBroadcast(intent);
        }

    };

    @Override
    public void onCreate() {

        super.onCreate();

    }
 //   @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        intival=intent.getIntExtra("intival",1);
        once=intent.getBooleanExtra("once",true);

        if(isServiceRunning==true){timer.cancel();}
        isServiceRunning = true;
        timer = new Timer();
        if (once==true)
        {
            try {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = 0;
                        timerhandler.sendMessage(msg);
                    }
                }, 1000);
            } catch (Exception e) {
e.printStackTrace();
            }
        }
        else {
            try {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = 0;
                        timerhandler.sendMessage(msg);
                    }
                }, 1000, intival * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    String GetHttpText(String url) {
        byte[] b = new byte[512];
        InputStream in = null;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            URL u = new URL(url);
            try {
                in = u.openStream();
                int i;
                while ((i = in.read(b)) != -1) {
                    bo.write(b, 0, i);
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bo.toString();

    }
}