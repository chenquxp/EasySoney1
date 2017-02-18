package com.chenqu.toolbox.easysoney;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.chenqu.toolbox.easysoney.EasySoneyActivity;

/**
 * Created by chenqu on 2017/2/9.
 */

public class PriceMonitorService extends Service {
    PowerManager.WakeLock mWakeLock;
    private EasySoneyActivity mESActivity;
    private Timer timer;
    private String exurl = "http://hq.sinajs.cn/list=sz159920";
    private String neturl = "http://hq.sinajs.cn/list=f_159920";
    private String tarurl = "http://hq.sinajs.cn/list=hkHSI";
    private String feurl = "http://hq.sinajs.cn/list=USDCNY";
    private int timercount = 0;
    private int notifyId = 100;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            try {
                timercount++;
                Intent intent = new Intent();
                String exvalue = GetHttpText(exurl);
                String netvalue = GetHttpText(neturl);
                String tarvalue = GetHttpText(tarurl);
                String fevalue = GetHttpText(feurl);
                ESData d = new ESData(exvalue, netvalue, tarvalue, fevalue, timercount);
                LogESData(d, "S");
                SendESNotify(d);
                // mESActivity.

                intent.putExtra("exvalue", exvalue);
                intent.putExtra("netvalue", netvalue);
                intent.putExtra("tarvalue", tarvalue);
                intent.putExtra("fevalue", fevalue);
                intent.putExtra("timercount", timercount);
                intent.setAction("com.chenqu.toolbox.easysoney.PriceMonitorService");
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private SharedPreferences read;
    Handler timerhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                new Thread(networkTask).start();
            } else if (msg.what == 1) {
                if (read.getBoolean("isautoonoff", false)) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Shanghai"));
                    if (!(IsBussinessTime(cal))) {
                        return;
                    }
                }
                new Thread(networkTask).start();
            }
        }
    };
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        //  acquireWakeLock();
        read = getSharedPreferences("pxmlfile", MODE_PRIVATE);
        editor = read.edit();
        initNotify();
        useForeground("EasySoney", "Foreground Quick Access");
    }

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
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

    public boolean IsBussinessTime(Calendar c) {

        SimpleDateFormat sdFormatter = new SimpleDateFormat("HH:mm:ss");
        String sNowTime = sdFormatter.format(c.getTime());
        if (!(sNowTime.compareTo("09:30:00") >= 0 && sNowTime.compareTo("11:30:00") <= 0 || sNowTime.compareTo("13:00:00") >= 0 && sNowTime.compareTo("15:00:00") <= 0)) {
            return false;
        }
        if (c.get(Calendar.DAY_OF_WEEK) < 2 || c.get(Calendar.DAY_OF_WEEK) > 6) {
            return false;
        }
        return true;
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
                    msg.what = 1;
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

    public String ReadFile(String filename) {
        try {
            FileInputStream inStream = this.openFileInput(filename);
            byte[] buffer = new byte[1024];
            int hasRead = 0;
            StringBuilder sb = new StringBuilder();
            while ((hasRead = inStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, hasRead));
            }
            inStream.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void WriteFile(String filename, String msg) {
        // 步骤1：获取输入值
        if (msg == null) return;
        try {
            // 步骤2:创建一个FileOutputStream对象,MODE_APPEND追加模式.重写用PRIVATE
            FileOutputStream fos = openFileOutput(filename, MODE_APPEND);
            // 步骤3：将获取过来的值放入文件
            fos.write(msg.getBytes());
            // 步骤4：关闭数据流
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LogESData(ESData d, String flag) {
        try {
            SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Shanghai"));
            String retStrFormatNowDate = sdFormatter.format(cal.getTime());
            String srecord = flag + ",";
            srecord += d.msCount + ",";
            srecord += retStrFormatNowDate + ",";
            srecord += d.msCurrentPrice + ",";
            srecord += d.msCurrentTime + ",";
            srecord += d.msLastdayNet + ",";
            srecord += d.msLastNetDate + ",";
            srecord += d.msLastTargetPrice + ",";
            srecord += d.msTargetCurrentTime + ",";
            srecord += d.msTargetCurrentPrice + ",";
            srecord += d.mDForeignExchangeIncreasePercent.toString().substring(0, 5) + "%,";
            srecord += d.mDMarginPercent.toString().substring(0, 5) + "%" + "\n" + d.errmsg;
            WriteFile("records.txt", srecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initNotify() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("iniNotify ContentTitle")
                .setContentText("iniNotify Contenttext")
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
                .setTicker("iniNotify Ticker")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_stat_name);
    }


    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    public void clearNotify(int notifyId) {
        mNotificationManager.cancel(notifyId);
    }


    public void clearAllNotify() {
        mNotificationManager.cancelAll();
    }

    public void showIntentActivityNotify(String stitle, String stext, String sticker) {
        mBuilder.setAutoCancel(true)
                .setContentTitle(stitle)
                .setContentText(stext)
                .setTicker(sticker);
        Intent resultIntent = new Intent(this, EasySoneyActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    public void SendESNotify(ESData d) {
        if (d.mDMarginPercent > 0 && d.mDMarginPercent < 90) {
            String smargin = (d.mDMarginPercent.toString().substring(0, 5) + "%");
            showIntentActivityNotify("Lucky time.", "Margin=" + smargin + " @" + d.msCurrentTime, "Margin=" + smargin + " @" + d.msCurrentTime);
        }
    }

    public Double CalcMarginPercent(String slastnet, String slasttarget, String scurrtarget, String scurrprice) {
        Double lasttarget;
        Double lastnet;
        Double currtarget;
        Double currprice;
        Double margin = 0.0;
        try {
            lastnet = Double.parseDouble(slastnet);
            lasttarget = Double.parseDouble(slasttarget);
            currtarget = Double.parseDouble(scurrtarget);
            currprice = Double.parseDouble(scurrprice);
            margin = (currtarget / lasttarget - currprice / lastnet - 0.0053) * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return margin;
    }

    public Double PredictNewNet(String slastnet, String slasttarget, String scurrtarget, String sfeincrease) {
        Double lasttarget;
        Double lastnet;
        Double currtarget;
        Double feincrease;
        Double newnet = 0.0;
        try {
            lastnet = Double.parseDouble(slastnet);
            lasttarget = Double.parseDouble(slasttarget);
            currtarget = Double.parseDouble(scurrtarget);
            feincrease = Double.parseDouble(sfeincrease.replace("%", ""));
            newnet = lastnet * (currtarget / lasttarget) * (1 + feincrease / 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newnet;
    }
    class MyBinder extends Binder {
        public PriceMonitorService getMyService() {
            return PriceMonitorService.this;
        }
    }

    class ESData {
        public String msCount;
        public String msCurrentPrice;
        public String msCurrentTime;
        public String msLastdayNet;
        public String msLastNetDate;
        public String msLastTargetPrice;
        public String msTargetCurrentTime;
        public String msTargetCurrentPrice;
        public Double mDMarginPercent;
        public Double mDForeignExchangeIncreasePercent;
        public int miLastTargetPriceTextColor;
        public int miLastdayNetTextColor;
        public String errmsg = "";

        ESData(String exval, String netval, String tarval, String feval, Integer count) {
            /*0：”大秦铁路”，股票名字；
1：”27.55″，今日开盘价；
2：”27.25″，昨日收盘价；
3：”26.91″，当前价格；
4：”27.55″，今日最高价；
5：”26.20″，今日最低价；
6：”26.91″，竞买价，即“买一”报价；
7：”26.92″，竞卖价，即“卖一”报价；
8：”22114263″，成交的股票数，由于股票交易以一百股为基本单位，所以在使用时，通常把该值除以一百；
9：”589824680″，成交金额，单位为“元”，为了一目了然，通常以“万元”为成交金额的单位，所以通常把该值除以一万；
10：”4695″，“买一”申请4695股，即47手；
11：”26.91″，“买一”报价；
12：”57590″，“买二”
13：”26.90″，“买二”
14：”14700″，“买三”
15：”26.89″，“买三”
16：”14300″，“买四”
17：”26.88″，“买四”
18：”15100″，“买五”
19：”26.87″，“买五”
20：”3100″，“卖一”申报3100股，即31手；
21：”26.92″，“卖一”报价
(22, 23), (24, 25), (26,27), (28, 29)分别为“卖二”至“卖四的情况”
30：”2008-01-11″，日期；
31：”15:05:32″，时间；
var hq_str_f_159920="恒生ETF(QDII),1.2784,1.2784,1.2609,2017-01-26,13.5156";
var hq_str_hkHSI="Hang Seng Main Index,恒生指数,23339.15,23374.17,23397.09,23307.05,23360.78,-13.39,-0.06,,,29584699,0,0.000,0.00,24364.00,18278.80,2017/01/27,12:09";
02 当日开盘
03 上日收盘
04 当日最高
05 当日最低
06 当前价

var hq_str_USDCNY="22:06:02,6.8742,6.8780,6.8648,136,6.8678,6.8742,6.8606,6.8742,美元人民币,2017-02-15";
var hq_str_USDCNY="15:38:03,6.8604,6.8654,6.8684,137,6.8686,6.8686,6.8549,6.8604,美元人民币,2017-02-16";
01 当前价，？，03今开，04波动点数，05昨收,06最高，07最低，?
*/
            String[] exsdata = exval.split(",");
            String[] netsdata = netval.split(",");
            String[] tarsdata = tarval.split(",");
            String[] fesdata = feval.split(",");
            // UI界面的更新等相关操作
            try {
                msCount = count.toString();
                msCurrentPrice = exsdata[7];
                msCurrentTime = exsdata[30] + " " + exsdata[31];
                msLastdayNet = netsdata[1];
                msLastNetDate = netsdata[4];
                msLastTargetPrice = tarsdata[3];
                msTargetCurrentTime = tarsdata[17].replace("/", "-") + " " + tarsdata[18].substring(0, 5);
                msTargetCurrentPrice = tarsdata[6];
                mDForeignExchangeIncreasePercent = (Double.parseDouble(fesdata[1]) / Double.parseDouble(fesdata[5]) - 1) * 100;
                String value = read.getString("target" + msLastNetDate, "");
                if (value.compareTo("") == 0) {
                    miLastTargetPriceTextColor = Color.BLUE;
                    errmsg += msLastNetDate + "Last Target Not Save err：" + "Null!=" + msLastTargetPrice + "\n";
                    if (read.getBoolean("isautosave", false)) {
                        if (msTargetCurrentTime.substring(0, 10).compareTo(msLastNetDate) == 0 && msTargetCurrentTime.substring(11, 16).compareTo("16:00") > 0) {
                            editor.putString("target" + msLastNetDate, msTargetCurrentPrice);
                            errmsg += msLastNetDate + "Last Target Auto Saved\n";
                        } else if (msTargetCurrentTime.substring(0, 10).compareTo(msLastNetDate) > 0) {
                            editor.putString("target" + msLastNetDate, msLastTargetPrice);
                            errmsg += msLastNetDate + "Last Target Auto Saved\n";
                        } else {
                            errmsg += msLastNetDate + "Last Target Attempt Auto Save Fail\n";
                        }
                    }
                } else if (value.compareTo(msLastTargetPrice) != 0) {
                    errmsg += (msLastNetDate + "Target Data err：" + value + "!=" + msLastTargetPrice + "\n");
                    msLastTargetPrice = value;
                    miLastTargetPriceTextColor = Color.RED;
                } else {
                    miLastTargetPriceTextColor = Color.BLACK;
                }
                value = read.getString("net" + msLastNetDate, "");
                if (value.compareTo("") == 0) {
                    miLastdayNetTextColor = Color.BLUE;
                    errmsg += msLastNetDate + "Last Net Not Save err：" + "Null!=" + msLastdayNet + "\n";
                    if (read.getBoolean("isautosave", false)) {
                        editor.putString("net" + msLastNetDate, msLastdayNet);
                        errmsg += msLastNetDate + "Last Net Auto Saved\n";
                    }
                } else if (value.compareTo(msLastdayNet) != 0) {
                    errmsg += (msLastNetDate + "Net Data err：" + value + "!=" + msLastdayNet + "\n");
                    miLastdayNetTextColor = Color.GREEN;
                } else {
                    miLastdayNetTextColor = Color.BLACK;
                }
                editor.commit();
                mDMarginPercent = CalcMarginPercent(msLastdayNet, msLastTargetPrice,
                        msTargetCurrentPrice, msCurrentPrice);
                errmsg += ("Data Refreshed\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
