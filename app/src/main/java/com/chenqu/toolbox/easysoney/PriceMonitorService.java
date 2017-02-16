package com.chenqu.toolbox.easysoney;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import com.chenqu.toolbox.easysoney.EasySoneyActivity.ESData;

/**
 * Created by chenqu on 2017/2/9.
 */

public class PriceMonitorService extends Service {
    PowerManager.WakeLock mWakeLock;
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
     * as given to {@link Context#bindService
     * Context.bindService}.  Note that any extras that were included with
     * the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    private EasySoneyActivity mESActivity;
    private Timer timer;
    private boolean once;
    private String exurl = "http://hq.sinajs.cn/list=sz159920";
    private String neturl = "http://hq.sinajs.cn/list=f_159920";
    private String tarurl = "http://hq.sinajs.cn/list=hkHSI";
    private String feurl = "http://hq.sinajs.cn/list=USDCNY";
    private int timercount = 0;
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            timercount++;
            Intent intent = new Intent();
            String exvalue = GetHttpText(exurl);
            String netvalue = GetHttpText(neturl);
            String tarvalue = GetHttpText(tarurl);
            String fevalue = GetHttpText(feurl);
            ESData d = mESActivity.new ESData(exvalue, netvalue, tarvalue, fevalue, timercount);
            mESActivity.LogESData(d, "S");
            mESActivity.SendESNotify(d);
            // TestWriteFile("records.txt", "ServiceWriteTest\n");

            intent.putExtra("exvalue", exvalue);
            intent.putExtra("netvalue", netvalue);
            intent.putExtra("tarvalue", tarvalue);
            intent.putExtra("fevalue", fevalue);
            intent.putExtra("timercount", timercount);
            intent.setAction("com.chenqu.toolbox.easysoney.PriceMonitorService");
            sendBroadcast(intent);

        }
    };
    Handler timerhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            new Thread(networkTask).start();
        }
    };

    //申请设备电源锁
    private void acquireWakeLock() {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "myWakeLock");
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p>
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     * <p>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     * start_compatibility}
     * <p>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //  acquireWakeLock();
        useForeground("EasySoney", "Foreground Quick Access");

    }

    public void useForeground(CharSequence tickerText, String contentText) {
        Intent notificationIntent = new Intent(getApplicationContext(), EasySoneyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
          /* Method 01
05.     * this method must SET SMALLICON!
06.     * otherwise it can't do what we want in Android 4.4 KitKat,
07.     * it can only show the application info page which contains the 'Force Close' button.*/
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(PriceMonitorService.this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setContentIntent(pendingIntent);
        Notification notification = mNotifyBuilder.build();

           /* Method 02
18.    Notification notification = new Notification(R.drawable.ic_launcher, tickerText,
19.            System.currentTimeMillis());
20.    notification.setLatestEventInfo(PlayService.this, getText(R.string.app_name),
21.            currSong, pendingIntent);
22.    */

        startForeground(101, notification);
    }

    @Override
    public MyBinder onBind(Intent intent) {

        return new MyBinder();
    }

    public void setMainActivity(EasySoneyActivity activity) {
        this.mESActivity = activity;
    }

    public void RefreshOnce() {
        try {
            Timer oncetimer = new Timer();
            oncetimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    timerhandler.sendMessage(msg);
                }
            }, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void RefreshContinous(int i) {
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    timerhandler.sendMessage(msg);
                }
            }, i * 1000, i * 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void StopContinous() {
        timer.cancel();
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //releaseWakeLock();
        stopForeground(true);
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

    public void TestWriteFile(String filename, String msg) {
        // 步骤1：获取输入值
        if (msg == null) return;
        try {
            // 步骤2:创建一个FileOutputStream对象,MODE_APPEND追加模式.重写用PRIVATE
            FileOutputStream fos = openFileOutput(filename,
                    MODE_APPEND);
            // 步骤3：将获取过来的值放入文件
            fos.write(msg.getBytes());
            // 步骤4：关闭数据流
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyBinder extends Binder {
        public PriceMonitorService getMyService() {
            return PriceMonitorService.this;
        }
    }
}
