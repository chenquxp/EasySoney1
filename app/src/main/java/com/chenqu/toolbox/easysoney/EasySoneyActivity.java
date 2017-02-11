package com.chenqu.toolbox.easysoney;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.*;
import android.graphics.*;

import java.util.*;
import java.io.*;
import java.text.*;

import com.chenqu.toolbox.easysoney.PriceMonitorService.*;
import com.chenqu.toolbox.easysoney.PriceMonitorService;


public class EasySoneyActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent mIntentService;

    private Button mBGetCurrentData;
    private Button mBSave;
    private Button mBLoad;
    private Button mBSaveTarget;
    private Button mBLoadTarget;
    private Button mBCalcMargin;
    private EditText mETCurrentPrice;
    private EditText mETCurrentTime;
    private EditText mETLastdayNet;

    private EditText mETLastNetDate;
    private EditText mETLastTargetPrice;
    private EditText mETTargetCurrentTime;
    private EditText mETTargetCurrentPrice;
    private EditText mETMargin;
    private ToggleButton mTBAuto;
    private TextView mTVIntivalSeconds;
    private TextView mTVCount;
    private SeekBar mSBIntivalSeconds;

    private Integer intivalseconds = 60;
    private boolean isBind = false;
    private PriceMonitorService mPriceService;
    private ESData mESdata;

    private int notifyId = 100;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    ServiceConnection sconn = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mPriceService = ((MyBinder) service).getMyService(); //获取Myservice对象
            mPriceService.setMainActivity(EasySoneyActivity.this); //把当前对象传递给myservice

            isBind = true;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.easy_soney_mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, ShowRecentDataActivity.class);
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.show_recent_data_item:
                //携带数据
                String starget = "";
                String snet = "";
                SharedPreferences read = getSharedPreferences("pxmlfile", MODE_WORLD_WRITEABLE);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date dt = format.parse(mETLastNetDate.getText().toString());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dt);
                    String t;
                    t = format.format(dt);
                    starget += "target" + t + ":" + read.getString("target" + t, "") + "\n";
                    snet += "net" + t + ":" + read.getString("net" + t, "") + "\n";

                    for (int i = 8; i >= 0; i--) {
                        calendar.add(Calendar.DATE, -1);
                        dt = calendar.getTime();
                        t = format.format(dt);
                        starget += "target" + t + ":" + read.getString("target" + t, "") + "\n";
                        snet += "net" + t + ":" + read.getString("net" + t, "") + "\n";
                    }
                    bundle.putString("text", starget + "\n" + snet);
                    bundle.putBoolean("isdelete", false);
                    //把附加的数据放到意图当中
                    intent.putExtras(bundle);
                    //执行意图
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    OutputMessage(e.toString());
                }
                break;
            case R.id.show_records_item:
                bundle.putString("text", ReadFile("records.txt"));
                bundle.putBoolean("isdelete", true);
                bundle.putString("filename", "records.txt");

                //把附加的数据放到意图当中
                intent.putExtras(bundle);
                //执行意图
                startActivity(intent);
                break;
            case R.id.exit_item:
                finish();
                break;
            default:
        }
        return true;

    }

    private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            intivalseconds = 30 + 30 * progress;
            mTVIntivalSeconds.setText("AutoGet Intival:" + intivalseconds.toString() + "Seconds");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_soney);
        mBGetCurrentData = (Button) findViewById(R.id.bt_getcurrdata);
        mBGetCurrentData.setOnClickListener(this);
        mBSave = (Button) findViewById(R.id.bt_savenet);
        mBSave.setOnClickListener(this);
        mBLoad = (Button) findViewById(R.id.bt_loadnet);
        mBLoad.setOnClickListener(this);
        mBLoadTarget = (Button) findViewById(R.id.bt_load_target);
        mBLoadTarget.setOnClickListener(this);
        mBSaveTarget = (Button) findViewById(R.id.bt_save_target);
        mBSaveTarget.setOnClickListener(this);
        mBCalcMargin = (Button) findViewById(R.id.bt_calc_margin);
        mBCalcMargin.setOnClickListener(this);
        mTBAuto = (ToggleButton) findViewById(R.id.tb_auto);
        mTBAuto.setOnClickListener(this);

        mTVIntivalSeconds = (TextView) findViewById(R.id.tv_intival_seconds);
        mTVCount = (TextView) findViewById(R.id.tv_count);
        mSBIntivalSeconds = (SeekBar) findViewById(R.id.sb_intival_seconds);
        mTVIntivalSeconds.setText("AutoGet Intival:" + intivalseconds.toString() + "Seconds");
        mSBIntivalSeconds.setOnSeekBarChangeListener(seekListener);

        mETCurrentPrice = (EditText) findViewById(R.id.et_current_price);
        mETCurrentTime = (EditText) findViewById(R.id.et_current_time);
        mETLastdayNet = (EditText) findViewById(R.id.et_lastday_net);
        mETLastNetDate = (EditText) findViewById(R.id.et_last_net_date);
        mETLastTargetPrice = (EditText) findViewById(R.id.et_last_target_price);
        mETTargetCurrentTime = (EditText) findViewById(R.id.et_target_current_time);
        mETTargetCurrentPrice = (EditText) findViewById(R.id.et_target_current_price);
        mETMargin = (EditText) findViewById(R.id.et_margin);
        SharedPreferences.Editor editor = getSharedPreferences("pxmlfile", MODE_WORLD_WRITEABLE).edit();
      /*  editor.putString("net2017-01-25", "1.2609");
        editor.putString("target2017-01-25", "23049.12");
        editor.commit();*/

        mBGetCurrentData.setFocusable(true);
        mBGetCurrentData.setFocusableInTouchMode(true);
        mBGetCurrentData.requestFocus();
        mBGetCurrentData.requestFocusFromTouch();

        initNotifyService();
        initNotify();
        registerReceiver();
        try {
            mIntentService = new Intent(EasySoneyActivity.this, PriceMonitorService.class);
            bindService(mIntentService, sconn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("系统提示");
            isExit.setMessage("确定要退出吗,点击取消转后台运行");
            isExit.setButton("确定", exitdialoglistener);
            isExit.setButton2("取消", exitdialoglistener);
            // 显示对话框
            isExit.show();
        }
        return false;
    }


    DialogInterface.OnClickListener exitdialoglistener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    moveTaskToBack(true);
                    break;
                default:
                    break;
            }
        }
    };

    Handler networkhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    public void OutputMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onClick(View v) {
        String date = mETLastNetDate.getText().toString().trim();
        String net = mETLastdayNet.getText().toString().trim();
        String target = mETLastTargetPrice.getText().toString().trim();
        String value = "";
        SharedPreferences.Editor editor = getSharedPreferences("pxmlfile", MODE_WORLD_WRITEABLE).edit();
        SharedPreferences read = getSharedPreferences("pxmlfile", MODE_WORLD_READABLE);
        switch (v.getId()) {
            case R.id.bt_getcurrdata:
                ClearData();
                if (isBind) {
                    mPriceService.RefreshOnce();
                }

                //     mIntentService = new Intent(EasySoneyActivity.this, PriceMonitorService.class);
                //     mIntentService.putExtra("intival", intivalseconds);
                //     mIntentService.putExtra("once", true);
                //     boolean b = bindService(mIntentService, sconn, Context.BIND_AUTO_CREATE);
                //    isBind = true;
                break;
            case R.id.bt_savenet:
                editor.putString("net" + date, net);
                editor.commit();
                OutputMessage("save complete net" + date + " " + net);
                break;
            case R.id.bt_loadnet:
                value = read.getString("net" + date, "");
                OutputMessage("saved data：" + value);
                mETLastdayNet.setText(value);
                break;
            case R.id.bt_save_target:
                editor.putString("target" + date, target);
                editor.commit();
                OutputMessage("save complete target" + date + " " + target);
                break;
            case R.id.bt_load_target:
                value = read.getString("target" + date, "");
                OutputMessage("saved data：" + value);
                mETLastTargetPrice.setText(value);
                break;
            case R.id.bt_calc_margin:
                //   CalcMargin();
                break;
            case R.id.tb_auto:

                if (mTBAuto.isChecked()) {
                    mSBIntivalSeconds.setEnabled(false);
                    if (isBind) {
                        mPriceService.RefreshContinous(intivalseconds);
                    }
                    //    mIntentService = new Intent(this, PriceMonitorService.class);
                    //      mIntentService.putExtra("intival", intivalseconds);
                    //     mIntentService.putExtra("once", false);
                    //    mIntentService.putExtra("stop", false);

                    //  isBind = true;
                } else {
                    mSBIntivalSeconds.setEnabled(true);
                    if (isBind) {
                        mPriceService.StopContinous();
                    }
                    //                    mIntentService = new Intent(this, PriceMonitorService.class);
                    //              mIntentService.putExtra("intival", intivalseconds);
                    //              mIntentService.putExtra("once", false);
                    //              mIntentService.putExtra("stop", true);

                }
                break;
        }

    }


    void ClearData() {
        mETCurrentPrice.setText("");
        mETCurrentTime.setText("");
        mETLastdayNet.setText("");
        mETLastNetDate.setText("");
        mETLastTargetPrice.setText("");
        mETTargetCurrentTime.setText("");
        mETTargetCurrentPrice.setText("");
        mETMargin.setText("");
    }

    private void initNotify() {
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


    public String CalcMargin(String slastnet, String slasttarget, String scurrtarget, String scurrprice, String scurrenttime) {
        Double lasttarget;
        Double lastnet;
        Double currtarget;
        Double currprice;
        Double margin;
        String smargin = "";
        try {
            lastnet = Double.parseDouble(slastnet);
            lasttarget = Double.parseDouble(slasttarget);
            currtarget = Double.parseDouble(scurrtarget);
            currprice = Double.parseDouble(scurrprice);

            margin = (currtarget / lasttarget - currprice / lastnet - 0.0053) * 100;
            smargin = (margin.toString().substring(0, 5) + "%");
            if (margin > 0.1) {

                showIntentActivityNotify("Lucky time.", "Margin=" + margin.toString().substring(0, 5) + "% @" + scurrenttime, "Margin=" + margin.toString().substring(0, 5) + "% @" + scurrenttime);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return smargin;
    }

    public void LogESData(ESData d) {
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Shanghai"));
        String retStrFormatNowDate = sdFormatter.format(cal.getTime());
        String srecord = "";
        srecord += d.msCount + ",";
        srecord += retStrFormatNowDate + ",";
        srecord += d.msCurrentPrice + ",";
        srecord += d.msCurrentTime + ",";
        srecord += d.msLastdayNet + ",";
        srecord += d.msLastNetDate + ",";
        srecord += d.msLastTargetPrice + ",";
        srecord += d.msTargetCurrentTime + ",";
        srecord += d.msTargetCurrentPrice + ",";
        srecord += d.msMargin + "\n";
        WriteFile("records.txt", srecord);
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }


    private void initNotifyService() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

    public void UpdateUI(ESData d) {

        // UI界面的更新等相关操作
        try {
            mTVCount.setText(d.msCount);
            mETCurrentPrice.setText(d.msCurrentPrice);
            mETCurrentTime.setText(d.msCurrentTime);
            mETLastdayNet.setText(d.msLastdayNet);
            mETLastNetDate.setText(d.msLastNetDate);
            mETLastTargetPrice.setText(d.msLastTargetPrice);
            mETTargetCurrentTime.setText(d.msTargetCurrentTime);
            mETTargetCurrentPrice.setText(d.msTargetCurrentPrice);
            mETMargin.setText(d.msMargin);
            mETLastTargetPrice.setTextColor(d.miLastTargetPriceTextColor);
            mETLastdayNet.setTextColor(d.miLastdayNetTextColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.chenqu.toolbox.easysoney.PriceMonitorService");
        EasySoneyActivity.this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();
                String exval = data.getString("exvalue");
                String netval = data.getString("netvalue");
                String tarval = data.getString("tarvalue");
                Integer count = data.getInt("timercount");
                ESData esdata = new ESData(exval, netval, tarval, count);
                LogESData(esdata);
                UpdateUI(esdata);
            }
        }, intentFilter);
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
        public String msMargin;
        public int miLastTargetPriceTextColor;
        public int miLastdayNetTextColor;

        ESData(String exval, String netval, String tarval, Integer count) {
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
*/
            String[] exsdata = exval.split(",");
            String[] netsdata = netval.split(",");
            String[] tarsdata = tarval.split(",");
            // UI界面的更新等相关操作
            try {
                msCount = count.toString();
                msCurrentPrice = exsdata[7];
                msCurrentTime = exsdata[30] + " " + exsdata[31];
                msLastdayNet = netsdata[1];
                msLastNetDate = netsdata[4];
                msLastTargetPrice = tarsdata[3];
                msTargetCurrentTime = tarsdata[17] + " " + tarsdata[18].substring(0, 5);
                msTargetCurrentPrice = tarsdata[6];

                SharedPreferences read = getSharedPreferences("pxmlfile", MODE_WORLD_READABLE);
                String value = read.getString("target" + mETLastNetDate.getText().toString(), "");
                if (value.compareTo("") == 0) {
                    miLastTargetPriceTextColor = Color.BLUE;
                    OutputMessage(msLastNetDate + "Last Target Not Save err：" + value + "!=" + msLastdayNet);
                } else if (value.compareTo(mETLastTargetPrice.getText().toString()) != 0) {
                    OutputMessage(mETLastNetDate.getText().toString() + "Data err：" + value + "!=" + msLastTargetPrice);
                    msLastTargetPrice = value;
                    miLastTargetPriceTextColor = Color.RED;
                } else {
                    OutputMessage("New data updated");
                    miLastTargetPriceTextColor = Color.BLACK;
                }
                value = read.getString("net" + msLastNetDate, "");
                if (value.compareTo(msLastdayNet) != 0) {
                    OutputMessage(msLastNetDate + "Last Net Not Save err：" + value + "!=" + msLastdayNet);
                    miLastdayNetTextColor = Color.BLUE;
                } else {
                    miLastdayNetTextColor = Color.BLACK;
                }
                msMargin = CalcMargin(msLastdayNet, msLastTargetPrice,
                        msTargetCurrentPrice, msCurrentPrice,
                        msCurrentTime);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
